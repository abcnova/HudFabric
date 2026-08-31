package dev.nova.hudfabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HudForgeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("hudfabric.json");

    public static final int SCOREBOARD_CUSTOM = 0;
    public static final int SCOREBOARD_VANILLA = 1;
    public static final int SCOREBOARD_HIDDEN = 2;
    public static final int CROSSHAIR_VANILLA = 0;
    public static final int CROSSHAIR_CROSS = 1;
    public static final int CROSSHAIR_CIRCLE = 2;
    public static final int CROSSHAIR_DOT = 3;

    public int configVersion = 14;
    public int scoreboardMode = SCOREBOARD_CUSTOM;
    public boolean scoreboardEnabled = true;
    public float scoreboardScale = 1.0f;
    public int scoreboardX = 300;
    public int scoreboardY = 34;
    public int scoreboardWidth = 168;
    public int scoreboardRadius = 0;
    public int scoreboardBackground = 0xB0101218;
    public int scoreboardAccent = 0xFF66E3FF;
    public int scoreboardText = 0xFFEAF0FF;
    public int scoreboardTitle = 0xFFFFFFFF;
    public boolean scoreboardServerColors = true;

    public boolean statsEnabled = true;
    public float statsScale = 1.0f;
    public int statsX = 8;
    public int statsY = 8;
    public int statsBackground = 0x98101218;
    public int statsAccent = 0xFF7C5CFF;
    public int statsText = 0xFFEAF0FF;
    public int statsRadius = 0;

    public boolean coordsEnabled = true;
    public float coordsScale = 1.0f;
    public int coordsX = 8;
    public int coordsY = 48;
    public int coordsWidth = 116;
    public int coordsRadius = 0;
    public int coordsBackground = 0x98101218;
    public int coordsText = 0xFFEAF0FF;

    public boolean effectsEnabled = true;
    public float effectsScale = 1.18f;
    public int effectsX = 140;
    public int effectsY = 8;
    public int effectsWidth = 150;
    public int effectsRadius = 4;
    public int effectsBackground = 0x98101218;
    public int effectsText = 0xFFEAF0FF;
    public boolean hideVanillaEffects = true;

    public boolean cpsEnabled = true;
    public float cpsScale = 1.0f;
    public int cpsX = 96;
    public int cpsY = 8;
    public int cpsWidth = 92;
    public int cpsRadius = 5;
    public int cpsBackground = 0x98101218;
    public int cpsText = 0xFFEAF0FF;

    public boolean equipmentEnabled = true;
    public float equipmentScale = 1.15f;
    public int equipmentX = 140;
    public int equipmentY = 100;
    public int equipmentWidth = 150;
    public int equipmentRadius = 4;
    public int equipmentBackground = 0x98101218;
    public int equipmentText = 0xFFEAF0FF;
    public int equipmentMode = 0;
    public int equipmentHotbarSide = 0;
    public int equipmentDurabilityDisplay = 0;
    public boolean equipmentWarning = true;
    public int equipmentWarningPercent = 10;
    public int equipmentHotbarX = -1;
    public int equipmentHotbarY = -1;
    public boolean equipmentHotbarBackground = true;
    public boolean equipmentHotbarSeparate = false;
    public int[] equipmentSlotX = {-1, -1, -1, -1};
    public int[] equipmentSlotY = {-1, -1, -1, -1};

    public boolean customCrosshair = true;
    public boolean hideVanillaCrosshair = true;
    public int crosshairMode = CROSSHAIR_CROSS;
    public int crosshairSize = 4;
    public int crosshairGap = 2;
    public int crosshairThickness = 1;
    public int crosshairColor = 0xFFEAF0FF;
    public boolean crosshairDot = false;
    public boolean crosshairOutline = true;
    public int crosshairOutlineThickness = 1;
    public int crosshairOutlineColor = 0xD0000000;
    public boolean crosshairIndicator = true;
    public boolean jumpResetEnabled = false;
    public int jumpResetMode = 0;
    public int jumpResetX = 210;
    public int jumpResetY = 90;
    public float jumpResetScale = 1.0f;
    public int jumpResetWidth = 128;
    public int jumpResetRadius = 6;
    public int jumpResetBackground = 0xC0101218;
    public int jumpResetText = 0xFFEAF0FF;
    public int jumpResetDisplayTicks = 50;

    public boolean hideVanillaScoreboard = true;
    public boolean autoRespawn = false;
    public boolean autoReconnect = false;
    public int autoRespawnDelayTicks = 30;
    public int autoReconnectDelaySeconds = 5;
    public boolean customBlockOutline = false;
    public int blockOutlineColor = 0xFF64A8FF;
    public Map<String, ServerProfile> serverProfiles = new LinkedHashMap<>();

    public static HudForgeConfig load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                HudForgeConfig loaded = GSON.fromJson(reader, HudForgeConfig.class);
                if (loaded != null) {
                    loaded.migrate();
                    return loaded.clamped();
                }
            } catch (IOException ignored) {
            }
        }

        HudForgeConfig created = new HudForgeConfig();
        created.save();
        return created;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(clamped(), writer);
            }
        } catch (IOException ignored) {
        }
    }

    public HudForgeConfig clamped() {
        if (!scoreboardEnabled) {
            scoreboardMode = hideVanillaScoreboard ? SCOREBOARD_HIDDEN : SCOREBOARD_VANILLA;
            scoreboardEnabled = true;
        }
        scoreboardMode = clamp(scoreboardMode, SCOREBOARD_CUSTOM, SCOREBOARD_HIDDEN);
        scoreboardScale = clamp(scoreboardScale, 0.5f, 2.5f);
        scoreboardX = clamp(scoreboardX, 0, 10000);
        scoreboardY = clamp(scoreboardY, 0, 10000);
        scoreboardWidth = clamp(scoreboardWidth, 90, 320);
        scoreboardRadius = clamp(scoreboardRadius, 0, 12);
        statsScale = clamp(statsScale, 0.5f, 2.5f);
        statsX = clamp(statsX, 0, 10000);
        statsY = clamp(statsY, 0, 10000);
        statsRadius = clamp(statsRadius, 0, 12);
        coordsScale = clamp(coordsScale, 0.5f, 2.5f);
        coordsX = clamp(coordsX, 0, 10000);
        coordsY = clamp(coordsY, 0, 10000);
        coordsWidth = clamp(coordsWidth, 92, 280);
        coordsRadius = clamp(coordsRadius, 0, 12);
        if (coordsBackground == 0 && coordsText == 0) {
            coordsBackground = 0x98101218;
            coordsText = 0xFFEAF0FF;
        }
        effectsScale = clamp(effectsScale, 0.5f, 2.5f);
        effectsX = clamp(effectsX, 0, 10000);
        effectsY = clamp(effectsY, 0, 10000);
        effectsWidth = clamp(effectsWidth, 110, 320);
        effectsRadius = clamp(effectsRadius, 0, 12);
        if (effectsBackground == 0 && effectsText == 0) {
            effectsBackground = 0x98101218;
            effectsText = 0xFFEAF0FF;
        }
        cpsScale = clamp(cpsScale, 0.5f, 2.5f);
        cpsX = clamp(cpsX, 0, 10000);
        cpsY = clamp(cpsY, 0, 10000);
        cpsWidth = clamp(cpsWidth, 72, 240);
        cpsRadius = clamp(cpsRadius, 0, 12);
        autoRespawnDelayTicks = clamp(autoRespawnDelayTicks, 20, 100);
        autoReconnectDelaySeconds = clamp(autoReconnectDelaySeconds, 1, 60);
        equipmentScale = clamp(equipmentScale, 0.5f, 2.5f);
        equipmentX = clamp(equipmentX, 0, 10000);
        equipmentY = clamp(equipmentY, 0, 10000);
        equipmentWidth = clamp(equipmentWidth, 120, 320);
        equipmentRadius = clamp(equipmentRadius, 0, 12);
        equipmentMode = clamp(equipmentMode, 0, 1);
        equipmentHotbarSide = clamp(equipmentHotbarSide, 0, 1);
        equipmentDurabilityDisplay = clamp(equipmentDurabilityDisplay, 0, 2);
        equipmentWarningPercent = clamp(equipmentWarningPercent, 1, 50);
        equipmentHotbarX = clamp(equipmentHotbarX, -1, 10000);
        equipmentHotbarY = clamp(equipmentHotbarY, -1, 10000);
        if (equipmentSlotX == null || equipmentSlotX.length != 4) equipmentSlotX = new int[]{-1, -1, -1, -1};
        if (equipmentSlotY == null || equipmentSlotY.length != 4) equipmentSlotY = new int[]{-1, -1, -1, -1};
        for (int i = 0; i < 4; i++) { equipmentSlotX[i] = clamp(equipmentSlotX[i], -1, 10000); equipmentSlotY[i] = clamp(equipmentSlotY[i], -1, 10000); }
        if (equipmentBackground == 0 && equipmentText == 0) {
            equipmentBackground = 0x98101218;
            equipmentText = 0xFFEAF0FF;
        }
        crosshairMode = clamp(crosshairMode, CROSSHAIR_VANILLA, CROSSHAIR_DOT);
        crosshairSize = clamp(crosshairSize, 2, 32);
        crosshairGap = clamp(crosshairGap, 0, 16);
        crosshairThickness = clamp(crosshairThickness, 1, 8);
        crosshairOutlineThickness = clamp(crosshairOutlineThickness, 1, 4);
        jumpResetMode = clamp(jumpResetMode, 0, 1); jumpResetX = clamp(jumpResetX, 0, 10000); jumpResetY = clamp(jumpResetY, 0, 10000);
        jumpResetWidth = clamp(jumpResetWidth, 92, 260); jumpResetRadius = clamp(jumpResetRadius, 0, 12);
        jumpResetScale = clamp(jumpResetScale, 0.5f, 2.5f); jumpResetDisplayTicks = clamp(jumpResetDisplayTicks, 20, 100);
        if (serverProfiles == null) {
            serverProfiles = new LinkedHashMap<>();
        }
        return this;
    }

    public void saveActiveProfile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        serverProfiles.put(key, ServerProfile.from(this));
    }

    public void applyProfile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        ServerProfile profile = serverProfiles.get(key);
        if (profile == null) {
            serverProfiles.put(key, ServerProfile.from(this));
            return;
        }
        profile.applyTo(this);
        clamped();
    }

    private void migrate() {
        if (configVersion < 2) {
            scoreboardMode = SCOREBOARD_CUSTOM;
            scoreboardBackground = 0xB0101218;
            scoreboardAccent = 0xFF66E3FF;
            scoreboardText = 0xFFEAF0FF;
            scoreboardTitle = 0xFFFFFFFF;
            statsBackground = 0x98101218;
            statsAccent = 0xFF7C5CFF;
            statsText = 0xFFEAF0FF;
            crosshairSize = 4;
            crosshairGap = 2;
            crosshairThickness = 1;
            crosshairColor = 0xFFEAF0FF;
            configVersion = 2;
            save();
        }
        if (configVersion < 3) {
            scoreboardAccent = 0xFF66E3FF;
            statsAccent = 0xFF7C5CFF;
            scoreboardRadius = 0;
            statsRadius = 0;
            crosshairDot = false;
            configVersion = 3;
            save();
        }
        if (configVersion < 4) {
            crosshairMode = customCrosshair ? CROSSHAIR_CROSS : CROSSHAIR_VANILLA;
            scoreboardRadius = 0;
            statsRadius = 0;
            configVersion = 4;
            save();
        }
        if (configVersion < 5) {
            scoreboardServerColors = true;
            autoRespawn = false;
            configVersion = 5;
            save();
        }
        if (configVersion < 6) {
            customBlockOutline = false;
            blockOutlineColor = 0xFF64A8FF;
            if (serverProfiles == null) {
                serverProfiles = new LinkedHashMap<>();
            }
            configVersion = 6;
            save();
        }
        if (configVersion < 7) {
            coordsEnabled = true;
            coordsScale = 1.0f;
            coordsX = 8;
            coordsY = 44;
            coordsWidth = 116;
            coordsRadius = 0;
            effectsEnabled = true;
            effectsScale = 1.0f;
            effectsX = 8;
            effectsY = 84;
            effectsWidth = 150;
            effectsRadius = 0;
            equipmentEnabled = true;
            equipmentScale = 1.0f;
            equipmentX = 8;
            equipmentY = 140;
            equipmentWidth = 150;
            equipmentRadius = 0;
            configVersion = 7;
            save();
        }
        if (configVersion < 8) {
            coordsBackground = 0x98101218;
            coordsText = 0xFFEAF0FF;
            coordsWidth = Math.max(coordsWidth, 116);
            effectsScale = Math.max(effectsScale, 1.18f);
            effectsWidth = Math.max(effectsWidth, 150);
            effectsRadius = Math.max(effectsRadius, 4);
            effectsBackground = 0x98101218;
            effectsText = 0xFFEAF0FF;
            equipmentScale = Math.max(equipmentScale, 1.15f);
            equipmentWidth = Math.max(equipmentWidth, 150);
            equipmentRadius = Math.max(equipmentRadius, 4);
            equipmentBackground = 0x98101218;
            equipmentText = 0xFFEAF0FF;
            autoReconnect = false;
            configVersion = 8;
            save();
        }
        if (configVersion < 9) {
            crosshairOutline = true;
            crosshairOutlineThickness = 1;
            crosshairOutlineColor = 0xD0000000;
            configVersion = 9;
            save();
        }
        if (configVersion < 10) {
            cpsEnabled = true;
            cpsScale = 1.0f;
            cpsX = 96;
            cpsY = 8;
            cpsWidth = 92;
            cpsRadius = 5;
            cpsBackground = 0x98101218;
            cpsText = 0xFFEAF0FF;
            autoRespawnDelayTicks = 30;
            autoReconnectDelaySeconds = 5;
            configVersion = 10;
            save();
        }
        if (configVersion < 11) {
            equipmentMode = 0;
            equipmentHotbarSide = 0;
            equipmentDurabilityDisplay = 0;
            equipmentWarning = true;
            equipmentWarningPercent = 10;
            configVersion = 11;
            save();
        }
        if (configVersion < 12) {
            equipmentHotbarX = -1;
            equipmentHotbarY = -1;
            equipmentHotbarBackground = true;
            crosshairIndicator = true;
            configVersion = 12;
            save();
        }
        if (configVersion < 13) {
            equipmentHotbarSeparate = false;
            equipmentSlotX = new int[]{-1, -1, -1, -1};
            equipmentSlotY = new int[]{-1, -1, -1, -1};
            configVersion = 13;
            save();
        }
        if (configVersion < 14) {
            jumpResetEnabled = false; jumpResetX = 210; jumpResetY = 90; jumpResetScale = 1.0f;
            jumpResetBackground = 0xC0101218; jumpResetText = 0xFFEAF0FF; jumpResetDisplayTicks = 50;
            configVersion = 14; save();
        }
        if (configVersion < 15) {
            jumpResetMode = 0; jumpResetWidth = 128; jumpResetRadius = 6; jumpResetDisplayTicks = 30;
            configVersion = 15; save();
        }
        if (configVersion < 16) {
            equipmentHotbarX = -1; equipmentHotbarY = -1;
            equipmentSlotX = new int[]{-1, -1, -1, -1}; equipmentSlotY = new int[]{-1, -1, -1, -1};
            configVersion = 16; save();
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class ServerProfile {
        public int scoreboardMode;
        public float scoreboardScale;
        public int scoreboardX;
        public int scoreboardY;
        public int scoreboardWidth;
        public int scoreboardRadius;
        public int scoreboardBackground;
        public int scoreboardText;
        public int scoreboardTitle;
        public boolean scoreboardServerColors;
        public boolean statsEnabled;
        public float statsScale;
        public int statsX;
        public int statsY;
        public int statsBackground;
        public int statsText;
        public int statsRadius;
        public boolean coordsEnabled;
        public float coordsScale;
        public int coordsX;
        public int coordsY;
        public int coordsWidth;
        public int coordsRadius;
        public int coordsBackground;
        public int coordsText;
        public boolean effectsEnabled;
        public float effectsScale;
        public int effectsX;
        public int effectsY;
        public int effectsWidth;
        public int effectsRadius;
        public int effectsBackground;
        public int effectsText;
        public boolean equipmentEnabled;
        public float equipmentScale;
        public int equipmentX;
        public int equipmentY;
        public int equipmentWidth;
        public int equipmentRadius;
        public int equipmentBackground;
        public int equipmentText;
        public int crosshairMode;
        public int crosshairSize;
        public int crosshairGap;
        public int crosshairThickness;
        public int crosshairColor;
        public boolean crosshairDot;
        public boolean crosshairOutline;
        public int crosshairOutlineThickness;
        public int crosshairOutlineColor;
        public boolean customBlockOutline;
        public int blockOutlineColor;

        static ServerProfile from(HudForgeConfig config) {
            ServerProfile profile = new ServerProfile();
            profile.scoreboardMode = config.scoreboardMode;
            profile.scoreboardScale = config.scoreboardScale;
            profile.scoreboardX = config.scoreboardX;
            profile.scoreboardY = config.scoreboardY;
            profile.scoreboardWidth = config.scoreboardWidth;
            profile.scoreboardRadius = config.scoreboardRadius;
            profile.scoreboardBackground = config.scoreboardBackground;
            profile.scoreboardText = config.scoreboardText;
            profile.scoreboardTitle = config.scoreboardTitle;
            profile.scoreboardServerColors = config.scoreboardServerColors;
            profile.statsEnabled = config.statsEnabled;
            profile.statsScale = config.statsScale;
            profile.statsX = config.statsX;
            profile.statsY = config.statsY;
            profile.statsBackground = config.statsBackground;
            profile.statsText = config.statsText;
            profile.statsRadius = config.statsRadius;
            profile.coordsEnabled = config.coordsEnabled;
            profile.coordsScale = config.coordsScale;
            profile.coordsX = config.coordsX;
            profile.coordsY = config.coordsY;
            profile.coordsWidth = config.coordsWidth;
            profile.coordsRadius = config.coordsRadius;
            profile.coordsBackground = config.coordsBackground;
            profile.coordsText = config.coordsText;
            profile.effectsEnabled = config.effectsEnabled;
            profile.effectsScale = config.effectsScale;
            profile.effectsX = config.effectsX;
            profile.effectsY = config.effectsY;
            profile.effectsWidth = config.effectsWidth;
            profile.effectsRadius = config.effectsRadius;
            profile.effectsBackground = config.effectsBackground;
            profile.effectsText = config.effectsText;
            profile.equipmentEnabled = config.equipmentEnabled;
            profile.equipmentScale = config.equipmentScale;
            profile.equipmentX = config.equipmentX;
            profile.equipmentY = config.equipmentY;
            profile.equipmentWidth = config.equipmentWidth;
            profile.equipmentRadius = config.equipmentRadius;
            profile.equipmentBackground = config.equipmentBackground;
            profile.equipmentText = config.equipmentText;
            profile.crosshairMode = config.crosshairMode;
            profile.crosshairSize = config.crosshairSize;
            profile.crosshairGap = config.crosshairGap;
            profile.crosshairThickness = config.crosshairThickness;
            profile.crosshairColor = config.crosshairColor;
            profile.crosshairDot = config.crosshairDot;
            profile.crosshairOutline = config.crosshairOutline;
            profile.crosshairOutlineThickness = config.crosshairOutlineThickness;
            profile.crosshairOutlineColor = config.crosshairOutlineColor;
            profile.customBlockOutline = config.customBlockOutline;
            profile.blockOutlineColor = config.blockOutlineColor;
            return profile;
        }

        void applyTo(HudForgeConfig config) {
            if (coordsScale <= 0.0f) {
                coordsEnabled = config.coordsEnabled;
                coordsScale = config.coordsScale;
                coordsX = config.coordsX;
                coordsY = config.coordsY;
                coordsWidth = config.coordsWidth;
                coordsRadius = config.coordsRadius;
                coordsBackground = config.coordsBackground;
                coordsText = config.coordsText;
            }
            if (effectsScale <= 0.0f) {
                effectsEnabled = config.effectsEnabled;
                effectsScale = config.effectsScale;
                effectsX = config.effectsX;
                effectsY = config.effectsY;
                effectsWidth = config.effectsWidth;
                effectsRadius = config.effectsRadius;
                effectsBackground = config.effectsBackground;
                effectsText = config.effectsText;
            }
            if (equipmentScale <= 0.0f) {
                equipmentEnabled = config.equipmentEnabled;
                equipmentScale = config.equipmentScale;
                equipmentX = config.equipmentX;
                equipmentY = config.equipmentY;
                equipmentWidth = config.equipmentWidth;
                equipmentRadius = config.equipmentRadius;
                equipmentBackground = config.equipmentBackground;
                equipmentText = config.equipmentText;
            }
            if (crosshairOutlineThickness <= 0) {
                crosshairOutline = config.crosshairOutline;
                crosshairOutlineThickness = config.crosshairOutlineThickness;
                crosshairOutlineColor = config.crosshairOutlineColor;
            }
            config.scoreboardMode = scoreboardMode;
            config.scoreboardScale = scoreboardScale;
            config.scoreboardX = scoreboardX;
            config.scoreboardY = scoreboardY;
            config.scoreboardWidth = scoreboardWidth;
            config.scoreboardRadius = scoreboardRadius;
            config.scoreboardBackground = scoreboardBackground;
            config.scoreboardText = scoreboardText;
            config.scoreboardTitle = scoreboardTitle;
            config.scoreboardServerColors = scoreboardServerColors;
            config.statsEnabled = statsEnabled;
            config.statsScale = statsScale;
            config.statsX = statsX;
            config.statsY = statsY;
            config.statsBackground = statsBackground;
            config.statsText = statsText;
            config.statsRadius = statsRadius;
            config.coordsEnabled = coordsEnabled;
            config.coordsScale = coordsScale;
            config.coordsX = coordsX;
            config.coordsY = coordsY;
            config.coordsWidth = coordsWidth;
            config.coordsRadius = coordsRadius;
            config.coordsBackground = coordsBackground;
            config.coordsText = coordsText;
            config.effectsEnabled = effectsEnabled;
            config.effectsScale = effectsScale;
            config.effectsX = effectsX;
            config.effectsY = effectsY;
            config.effectsWidth = effectsWidth;
            config.effectsRadius = effectsRadius;
            config.effectsBackground = effectsBackground;
            config.effectsText = effectsText;
            config.equipmentEnabled = equipmentEnabled;
            config.equipmentScale = equipmentScale;
            config.equipmentX = equipmentX;
            config.equipmentY = equipmentY;
            config.equipmentWidth = equipmentWidth;
            config.equipmentRadius = equipmentRadius;
            config.equipmentBackground = equipmentBackground;
            config.equipmentText = equipmentText;
            config.crosshairMode = crosshairMode;
            config.crosshairSize = crosshairSize;
            config.crosshairGap = crosshairGap;
            config.crosshairThickness = crosshairThickness;
            config.crosshairColor = crosshairColor;
            config.crosshairDot = crosshairDot;
            config.crosshairOutline = crosshairOutline;
            config.crosshairOutlineThickness = crosshairOutlineThickness;
            config.crosshairOutlineColor = crosshairOutlineColor;
            // Block outline is a global visual preference and must not silently
            // change when joining a server with an older profile.
        }
    }
}
