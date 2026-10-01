package com.pixelgesture;

/** Small preference policy, independent of Activity and gesture state. */
public final class ThemeMode {
    public static final String KEY = "theme_mode";
    public static final String SYSTEM = "system", LIGHT = "light", DARK = "dark";
    private ThemeMode() { }
    public static String normalize(String value) {
        return LIGHT.equals(value) || DARK.equals(value) ? value : SYSTEM;
    }
    public static int apply(String mode, int uiMode, int systemUiMode) {
        int night = LIGHT.equals(mode) ? 0x10 : DARK.equals(mode) ? 0x20 : (systemUiMode & 0x30);
        return (uiMode & ~0x30) | night;
    }
}
