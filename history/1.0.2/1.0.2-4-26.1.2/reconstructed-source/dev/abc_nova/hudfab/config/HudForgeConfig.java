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
    public int configVersion = 6;
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
    public boolean customCrosshair = true;
    public boolean hideVanillaCrosshair = true;
    public int crosshairMode = 1;
    public int crosshairSize = 4;
    public int crosshairGap = 2;
    public int crosshairThickness = 1;
    public int crosshairColor = -1380097;
    public boolean crosshairDot = false;
    public boolean hideVanillaScoreboard = true;
    public boolean autoRespawn = false;
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
        this.crosshairMode = HudForgeConfig.clamp(this.crosshairMode, 0, 3);
        this.crosshairSize = HudForgeConfig.clamp(this.crosshairSize, 2, 32);
        this.crosshairGap = HudForgeConfig.clamp(this.crosshairGap, 0, 16);
        this.crosshairThickness = HudForgeConfig.clamp(this.crosshairThickness, 1, 8);
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
        public int crosshairMode;
        public int crosshairSize;
        public int crosshairGap;
        public int crosshairThickness;
        public int crosshairColor;
        public boolean crosshairDot;
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
            profile.crosshairMode = config.crosshairMode;
            profile.crosshairSize = config.crosshairSize;
            profile.crosshairGap = config.crosshairGap;
            profile.crosshairThickness = config.crosshairThickness;
            profile.crosshairColor = config.crosshairColor;
            profile.crosshairDot = config.crosshairDot;
            profile.customBlockOutline = config.customBlockOutline;
            profile.blockOutlineColor = config.blockOutlineColor;
            return profile;
        }

        void applyTo(HudForgeConfig config) {
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
            config.crosshairMode = this.crosshairMode;
            config.crosshairSize = this.crosshairSize;
            config.crosshairGap = this.crosshairGap;
            config.crosshairThickness = this.crosshairThickness;
            config.crosshairColor = this.crosshairColor;
            config.crosshairDot = this.crosshairDot;
            config.customBlockOutline = this.customBlockOutline;
            config.blockOutlineColor = this.blockOutlineColor;
        }
    }
}

