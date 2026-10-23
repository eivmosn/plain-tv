package io.github.op7418.guizangtv;

/** Shared validation for old/corrupt preferences, independent of Android. */
final class AppearanceValues {
    static int defaultValue(String key) {
        if ("size".equals(key) || "dim".equals(key)) return 1;
        if ("focus".equals(key)) return 1;
        if ("apps".equals(key)) return 2;
        if ("controls".equals(key)) return 1;
        return 0;
    }
    static int normalize(String key, int value) {
        int count;
        switch (key) {
            case "layout": case "focus": count = 2; break;
            case "background": case "apps": case "controls": case "font": case "size":
            case "material": case "dim": count = 3; break;
            default: return 0;
        }
        return value >= 0 && value < count ? value : defaultValue(key);
    }
}
