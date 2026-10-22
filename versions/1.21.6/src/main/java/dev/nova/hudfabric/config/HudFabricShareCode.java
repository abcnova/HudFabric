package dev.nova.hudfabric.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/** Encodes shareable HUD preferences without including server addresses or profiles. */
public final class HudFabricShareCode {
    private static final String PREFIX = "HUDFABRIC1:";
    private static final int MAX_CODE_LENGTH = 64_000;
    private static final Gson GSON = new GsonBuilder().create();

    private HudFabricShareCode() { }

    public static String encode(HudFabricConfig config, Map<String, String> keybindings) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", 1);
        JsonObject settings = GSON.toJsonTree(config).getAsJsonObject();
        settings.remove("serverProfiles");
        root.add("settings", settings);
        root.add("keybindings", GSON.toJsonTree(keybindings));
        return PREFIX + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
    }

    public static Map<String, String> decodeAndApply(String code, HudFabricConfig current) {
        String value = code == null ? "" : code.trim();
        if (!value.startsWith(PREFIX) || value.length() > MAX_CODE_LENGTH) {
            throw new IllegalArgumentException("This is not a valid HudFabric share code.");
        }

        try {
            byte[] bytes = Base64.getUrlDecoder().decode(value.substring(PREFIX.length()));
            JsonObject root = JsonParser.parseString(new String(bytes, StandardCharsets.UTF_8)).getAsJsonObject();
            if (!root.has("schema") || root.get("schema").getAsInt() != 1 || !root.has("settings")) {
                throw new IllegalArgumentException("This HudFabric share code uses an unsupported format.");
            }

            HudFabricConfig imported = GSON.fromJson(root.get("settings"), HudFabricConfig.class);
            if (imported == null) throw new IllegalArgumentException("The share code contains no settings.");
            for (Field field : HudFabricConfig.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.getName().equals("serverProfiles")) continue;
                Object next = field.get(imported);
                field.set(current, next instanceof int[] array ? array.clone() : next);
            }
            current.clamped().save();

            Map<String, String> keys = new LinkedHashMap<>();
            if (root.has("keybindings") && root.get("keybindings").isJsonObject()) {
                for (Map.Entry<String, com.google.gson.JsonElement> entry : root.getAsJsonObject("keybindings").entrySet()) {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                        keys.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
            return keys;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalArgumentException("Could not read this HudFabric share code.", e);
        }
    }
}
