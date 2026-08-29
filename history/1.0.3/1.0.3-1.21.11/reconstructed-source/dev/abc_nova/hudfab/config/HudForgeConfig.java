/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  net.fabricmc.loader.api.FabricLoader
 */
package dev.abc_nova.hudfab.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;

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
    public int configVersion = 9;
    public int scoreboardMode = 0;
    public boolean scoreboardEnabled = true;
    public float scoreboardScale = 1.0f;
    public int scoreboardX = 10;
    public int scoreboardY = 34;
    public int scoreboardWidth = 168;
    public int scoreboardRadius = 0;
    public int scoreboardBackground = -1341124072;
    public int scoreboardAccent = -10034177;
    public int scoreboardText = -1380097;
    public int scoreboardTitle = -1;
    public boolean scoreboardServerColors = true;
    public boolean statsEnabled = true;
    public float statsScale = 1.0f;
    public int statsX = 8;
    public int statsY = 8;
    public int statsBackground = -1743777256;
    public int statsAccent = -8626945;
    public int statsText = -1380097;
    public int statsRadius = 0;
    public boolean coordsEnabled = true;
    public float coordsScale = 1.0f;
    public int coordsX = 8;
    public int coordsY = 44;
    public int coordsWidth = 116;
    public int coordsRadius = 0;
    public int coordsBackground = -1743777256;
    public int coordsText = -1380097;
    public boolean effectsEnabled = true;
    public float effectsScale = 1.18f;
    public int effectsX = 8;
    public int effectsY = 90;
    public int effectsWidth = 150;
    public int effectsRadius = 4;
    public int effectsBackground = -1743777256;
    public int effectsText = -1380097;
    public boolean hideVanillaEffects = true;
    public boolean equipmentEnabled = true;
    public float equipmentScale = 1.15f;
    public int equipmentX = 8;
    public int equipmentY = 164;
    public int equipmentWidth = 150;
    public int equipmentRadius = 4;
    public int equipmentBackground = -1743777256;
    public int equipmentText = -1380097;
    public boolean customCrosshair = true;
    public boolean hideVanillaCrosshair = true;
    public int crosshairMode = 1;
    public int crosshairSize = 4;
    public int crosshairGap = 2;
    public int crosshairThickness = 1;
    public int crosshairColor = -1380097;
    public boolean crosshairDot = false;
    public boolean crosshairOutline = true;
    public int crosshairOutlineThickness = 1;
    public int crosshairOutlineColor = -805306368;
    public boolean hideVanillaScoreboard = true;
    public boolean autoRespawn = false;
    public boolean autoReconnect = false;
    public boolean customBlockOutline = false;
    public int blockOutlineColor = -10180353;
    public Map<String, ServerProfile> serverProfiles = new LinkedHashMap<String, ServerProfile>();

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static HudForgeConfig load() {
        if (Files.exists(PATH, new LinkOption[0])) {
            try (BufferedReader reader2222 = Files.newBufferedReader(PATH);){
                HudForgeConfig loaded = (HudForgeConfig)GSON.fromJson((Reader)reader2222, HudForgeConfig.class);
                if (loaded != null) {
                    loaded.migrate();
                    HudForgeConfig hudForgeConfig = loaded.clamped();
                    return hudForgeConfig;
                }
            }
            catch (IOException reader2222) {
                // empty catch block
            }
        }
        HudForgeConfig created = new HudForgeConfig();
        created.save();
        return created;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent(), new FileAttribute[0]);
            try (BufferedWriter writer = Files.newBufferedWriter(PATH, new OpenOption[0]);){
                GSON.toJson((Object)this.clamped(), (Appendable)writer);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public HudForgeConfig clamped() {
        if (!this.scoreboardEnabled) {
            this.scoreboardMode = this.hideVanillaScoreboard ? 2 : 1;
            this.scoreboardEnabled = true;
        }
        this.scoreboardMode = HudForgeConfig.clamp(this.scoreboardMode, 0, 2);
        this.scoreboardScale = HudForgeConfig.clamp(this.scoreboardScale, 0.5f, 2.5f);
        this.scoreboardX = HudForgeConfig.clamp(this.scoreboardX, 0, 10000);
        this.scoreboardY = HudForgeConfig.clamp(this.scoreboardY, 0, 10000);
        this.scoreboardWidth = HudForgeConfig.clamp(this.scoreboardWidth, 90, 320);
        this.scoreboardRadius = HudForgeConfig.clamp(this.scoreboardRadius, 0, 12);
        this.statsScale = HudForgeConfig.clamp(this.statsScale, 0.5f, 2.5f);
        this.statsX = HudForgeConfig.clamp(this.statsX, 0, 10000);
        this.statsY = HudForgeConfig.clamp(this.statsY, 0, 10000);
        this.statsRadius = HudForgeConfig.clamp(this.statsRadius, 0, 12);
        this.coordsScale = HudForgeConfig.clamp(this.coordsScale, 0.5f, 2.5f);
        this.coordsX = HudForgeConfig.clamp(this.coordsX, 0, 10000);
        this.coordsY = HudForgeConfig.clamp(this.coordsY, 0, 10000);
        this.coordsWidth = HudForgeConfig.clamp(this.coordsWidth, 92, 280);
        this.coordsRadius = HudForgeConfig.clamp(this.coordsRadius, 0, 12);
        if (this.coordsBackground == 0 && this.coordsText == 0) {
            this.coordsBackground = -1743777256;
            this.coordsText = -1380097;
        }
        this.effectsScale = HudForgeConfig.clamp(this.effectsScale, 0.5f, 2.5f);
        this.effectsX = HudForgeConfig.clamp(this.effectsX, 0, 10000);
        this.effectsY = HudForgeConfig.clamp(this.effectsY, 0, 10000);
        this.effectsWidth = HudForgeConfig.clamp(this.effectsWidth, 110, 320);
        this.effectsRadius = HudForgeConfig.clamp(this.effectsRadius, 0, 12);
        if (this.effectsBackground == 0 && this.effectsText == 0) {
            this.effectsBackground = -1743777256;
            this.effectsText = -1380097;
        }
        this.equipmentScale = HudForgeConfig.clamp(this.equipmentScale, 0.5f, 2.5f);
        this.equipmentX = HudForgeConfig.clamp(this.equipmentX, 0, 10000);
        this.equipmentY = HudForgeConfig.clamp(this.equipmentY, 0, 10000);
        this.equipmentWidth = HudForgeConfig.clamp(this.equipmentWidth, 120, 320);
        this.equipmentRadius = HudForgeConfig.clamp(this.equipmentRadius, 0, 12);
        if (this.equipmentBackground == 0 && this.equipmentText == 0) {
            this.equipmentBackground = -1743777256;
            this.equipmentText = -1380097;
        }
        this.crosshairMode = HudForgeConfig.clamp(this.crosshairMode, 0, 3);
        this.crosshairSize = HudForgeConfig.clamp(this.crosshairSize, 2, 32);
        this.crosshairGap = HudForgeConfig.clamp(this.crosshairGap, 0, 16);
        this.crosshairThickness = HudForgeConfig.clamp(this.crosshairThickness, 1, 8);
        this.crosshairOutlineThickness = HudForgeConfig.clamp(this.crosshairOutlineThickness, 1, 4);
        if (this.serverProfiles == null) {
            this.serverProfiles = new LinkedHashMap<String, ServerProfile>();
        }
        return this;
    }

    public void saveActiveProfile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        this.serverProfiles.put(key, ServerProfile.from(this));
    }

    public void applyProfile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        ServerProfile profile = this.serverProfiles.get(key);
        if (profile == null) {
            this.serverProfiles.put(key, ServerProfile.from(this));
            return;
        }
        profile.applyTo(this);
        this.clamped();
    }

    private void migrate() {
        if (this.configVersion < 2) {
            this.scoreboardMode = 0;
            this.scoreboardBackground = -1341124072;
            this.scoreboardAccent = -10034177;
            this.scoreboardText = -1380097;
            this.scoreboardTitle = -1;
            this.statsBackground = -1743777256;
            this.statsAccent = -8626945;
            this.statsText = -1380097;
            this.crosshairSize = 4;
            this.crosshairGap = 2;
            this.crosshairThickness = 1;
            this.crosshairColor = -1380097;
            this.configVersion = 2;
            this.save();
        }
        if (this.configVersion < 3) {
            this.scoreboardAccent = -10034177;
            this.statsAccent = -8626945;
            this.scoreboardRadius = 0;
            this.statsRadius = 0;
            this.crosshairDot = false;
            this.configVersion = 3;
            this.save();
        }
        if (this.configVersion < 4) {
            this.crosshairMode = this.customCrosshair ? 1 : 0;
            this.scoreboardRadius = 0;
            this.statsRadius = 0;
            this.configVersion = 4;
            this.save();
        }
        if (this.configVersion < 5) {
            this.scoreboardServerColors = true;
            this.autoRespawn = false;
            this.configVersion = 5;
            this.save();
        }
        if (this.configVersion < 6) {
            this.customBlockOutline = false;
            this.blockOutlineColor = -10180353;
            if (this.serverProfiles == null) {
                this.serverProfiles = new LinkedHashMap<String, ServerProfile>();
            }
            this.configVersion = 6;
            this.save();
        }
        if (this.configVersion < 7) {
            this.coordsEnabled = true;
            this.coordsScale = 1.0f;
            this.coordsX = 8;
            this.coordsY = 44;
            this.coordsWidth = 116;
            this.coordsRadius = 0;
            this.effectsEnabled = true;
            this.effectsScale = 1.0f;
            this.effectsX = 8;
            this.effectsY = 84;
            this.effectsWidth = 150;
            this.effectsRadius = 0;
            this.equipmentEnabled = true;
            this.equipmentScale = 1.0f;
            this.equipmentX = 8;
            this.equipmentY = 140;
            this.equipmentWidth = 150;
            this.equipmentRadius = 0;
            this.configVersion = 7;
            this.save();
        }
        if (this.configVersion < 8) {
            this.coordsBackground = -1743777256;
            this.coordsText = -1380097;
            this.coordsWidth = Math.max(this.coordsWidth, 116);
            this.effectsScale = Math.max(this.effectsScale, 1.18f);
            this.effectsWidth = Math.max(this.effectsWidth, 150);
            this.effectsRadius = Math.max(this.effectsRadius, 4);
            this.effectsBackground = -1743777256;
            this.effectsText = -1380097;
            this.equipmentScale = Math.max(this.equipmentScale, 1.15f);
            this.equipmentWidth = Math.max(this.equipmentWidth, 150);
            this.equipmentRadius = Math.max(this.equipmentRadius, 4);
            this.equipmentBackground = -1743777256;
            this.equipmentText = -1380097;
            this.autoReconnect = false;
            this.configVersion = 8;
            this.save();
        }
        if (this.configVersion < 9) {
            this.crosshairOutline = true;
            this.crosshairOutlineThickness = 1;
            this.crosshairOutlineColor = -805306368;
            this.configVersion = 9;
            this.save();
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
            if (this.coordsScale <= 0.0f) {
                this.coordsEnabled = config.coordsEnabled;
                this.coordsScale = config.coordsScale;
                this.coordsX = config.coordsX;
                this.coordsY = config.coordsY;
                this.coordsWidth = config.coordsWidth;
                this.coordsRadius = config.coordsRadius;
                this.coordsBackground = config.coordsBackground;
                this.coordsText = config.coordsText;
            }
            if (this.effectsScale <= 0.0f) {
                this.effectsEnabled = config.effectsEnabled;
                this.effectsScale = config.effectsScale;
                this.effectsX = config.effectsX;
                this.effectsY = config.effectsY;
                this.effectsWidth = config.effectsWidth;
                this.effectsRadius = config.effectsRadius;
                this.effectsBackground = config.effectsBackground;
                this.effectsText = config.effectsText;
            }
            if (this.equipmentScale <= 0.0f) {
                this.equipmentEnabled = config.equipmentEnabled;
                this.equipmentScale = config.equipmentScale;
                this.equipmentX = config.equipmentX;
                this.equipmentY = config.equipmentY;
                this.equipmentWidth = config.equipmentWidth;
                this.equipmentRadius = config.equipmentRadius;
                this.equipmentBackground = config.equipmentBackground;
                this.equipmentText = config.equipmentText;
            }
            if (this.crosshairOutlineThickness <= 0) {
                this.crosshairOutline = config.crosshairOutline;
                this.crosshairOutlineThickness = config.crosshairOutlineThickness;
                this.crosshairOutlineColor = config.crosshairOutlineColor;
            }
            config.scoreboardMode = this.scoreboardMode;
            config.scoreboardScale = this.scoreboardScale;
            config.scoreboardX = this.scoreboardX;
            config.scoreboardY = this.scoreboardY;
            config.scoreboardWidth = this.scoreboardWidth;
            config.scoreboardRadius = this.scoreboardRadius;
            config.scoreboardBackground = this.scoreboardBackground;
            config.scoreboardText = this.scoreboardText;
            config.scoreboardTitle = this.scoreboardTitle;
            config.scoreboardServerColors = this.scoreboardServerColors;
            config.statsEnabled = this.statsEnabled;
            config.statsScale = this.statsScale;
            config.statsX = this.statsX;
            config.statsY = this.statsY;
            config.statsBackground = this.statsBackground;
            config.statsText = this.statsText;
            config.statsRadius = this.statsRadius;
            config.coordsEnabled = this.coordsEnabled;
            config.coordsScale = this.coordsScale;
            config.coordsX = this.coordsX;
            config.coordsY = this.coordsY;
            config.coordsWidth = this.coordsWidth;
            config.coordsRadius = this.coordsRadius;
            config.coordsBackground = this.coordsBackground;
            config.coordsText = this.coordsText;
            config.effectsEnabled = this.effectsEnabled;
            config.effectsScale = this.effectsScale;
            config.effectsX = this.effectsX;
            config.effectsY = this.effectsY;
            config.effectsWidth = this.effectsWidth;
            config.effectsRadius = this.effectsRadius;
            config.effectsBackground = this.effectsBackground;
            config.effectsText = this.effectsText;
            config.equipmentEnabled = this.equipmentEnabled;
            config.equipmentScale = this.equipmentScale;
            config.equipmentX = this.equipmentX;
            config.equipmentY = this.equipmentY;
            config.equipmentWidth = this.equipmentWidth;
            config.equipmentRadius = this.equipmentRadius;
            config.equipmentBackground = this.equipmentBackground;
            config.equipmentText = this.equipmentText;
            config.crosshairMode = this.crosshairMode;
            config.crosshairSize = this.crosshairSize;
            config.crosshairGap = this.crosshairGap;
            config.crosshairThickness = this.crosshairThickness;
            config.crosshairColor = this.crosshairColor;
            config.crosshairDot = this.crosshairDot;
            config.crosshairOutline = this.crosshairOutline;
            config.crosshairOutlineThickness = this.crosshairOutlineThickness;
            config.crosshairOutlineColor = this.crosshairOutlineColor;
        }
    }
}

