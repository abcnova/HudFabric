package dev.nova.hudfabric.render;

/** Builds safe vanilla effect-icon paths without letting malformed mod keys crash HUD rendering. */
final class EffectTexturePath {
    private EffectTexturePath() {
    }

    static String fromPath(String path) {
        if (!isValidResourcePath(path)) {
            return "";
        }
        return "textures/mob_effect/" + path + ".png";
    }

    private static boolean isValidResourcePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (!((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '/' || c == '.' || c == '_' || c == '-')) {
                return false;
            }
        }
        return true;
    }
}