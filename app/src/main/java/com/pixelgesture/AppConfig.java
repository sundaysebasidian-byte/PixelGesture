package com.pixelgesture;

import android.content.SharedPreferences;

/** Shared preference keys and human-readable action choices. */
public final class AppConfig {
    private AppConfig() {}

    public static final String PREFS = "pixel_gesture_preferences";
    public static final String EDGE_WIDTH = "edge_width";
    public static final String HAPTIC = "haptic_enabled";
    public static final String MASTER_ENABLED = "master_enabled";
    public static final String LANGUAGE = "language";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_CHINESE = "zh";
    static final String CONFIG_VERSION = "config_version";
    static final int CURRENT_CONFIG_VERSION = 2;

    public static final String LEFT_IN = "left_in";
    public static final String LEFT_UP = "left_up";
    public static final String LEFT_DOWN = "left_down";
    public static final String LEFT_DOUBLE = "left_double";
    public static final String LEFT_TRIPLE = "left_triple";
    public static final String LEFT_LONG = "left_long";
    public static final String LEFT_IN_UP = "left_in_up";
    public static final String LEFT_IN_DOWN = "left_in_down";
    public static final String RIGHT_IN = "right_in";
    public static final String RIGHT_UP = "right_up";
    public static final String RIGHT_DOWN = "right_down";
    public static final String RIGHT_DOUBLE = "right_double";
    public static final String RIGHT_TRIPLE = "right_triple";
    public static final String RIGHT_LONG = "right_long";
    public static final String RIGHT_IN_UP = "right_in_up";
    public static final String RIGHT_IN_DOWN = "right_in_down";
    public static final String BOTTOM_UP = "bottom_up";
    public static final String BOTTOM_LEFT = "bottom_left";
    public static final String BOTTOM_RIGHT = "bottom_right";
    public static final String BOTTOM_UP_LEFT = "bottom_up_left";
    public static final String BOTTOM_UP_RIGHT = "bottom_up_right";
    public static final String BOTTOM_LONG = "bottom_long";
    public static final String BOTTOM_DOUBLE = "bottom_double";
    public static final String BOTTOM_TRIPLE = "bottom_triple";
    public static final String TOP_LEFT_DOWN = "top_left_down";
    public static final String TOP_LEFT_DOUBLE = "top_left_double";
    public static final String TOP_LEFT_LONG = "top_left_long";
    public static final String TOP_RIGHT_DOWN = "top_right_down";
    public static final String TOP_RIGHT_DOUBLE = "top_right_double";
    public static final String TOP_RIGHT_LONG = "top_right_long";
    public static final String SHAKE = "shake";
    public static final String VOLUME_CHORD = "volume_chord";
    public static final String VOLUME_UP_DOUBLE = "volume_up_double";
    public static final String VOLUME_DOWN_DOUBLE = "volume_down_double";
    public static final String VOLUME_UP_LONG = "volume_up_long";
    public static final String VOLUME_DOWN_LONG = "volume_down_long";

    public static final String BACK = "back";
    public static final String HOME = "home";
    public static final String RECENTS = "recents";
    public static final String NOTIFICATIONS = "notifications";
    public static final String QUICK_SETTINGS = "quick_settings";
    public static final String SCREENSHOT = "screenshot";
    public static final String LOCK = "lock";
    public static final String POWER_DIALOG = "power_dialog";
    public static final String CAMERA = "camera";
    public static final String OPEN_APP = "open_app";
    public static final String SETTINGS = "settings";
    public static final String NONE = "none";

    public static final String[] ACTION_KEYS = {
            NONE, BACK, HOME, RECENTS, NOTIFICATIONS, QUICK_SETTINGS,
            SCREENSHOT, LOCK, POWER_DIALOG, CAMERA, OPEN_APP, SETTINGS
    };

    public static final String[] LEFT_GESTURES = {
            LEFT_IN, LEFT_UP, LEFT_DOWN, LEFT_DOUBLE, LEFT_TRIPLE, LEFT_LONG, LEFT_IN_UP, LEFT_IN_DOWN
    };
    public static final String[] RIGHT_GESTURES = {
            RIGHT_IN, RIGHT_UP, RIGHT_DOWN, RIGHT_DOUBLE, RIGHT_TRIPLE, RIGHT_LONG, RIGHT_IN_UP, RIGHT_IN_DOWN
    };
    public static final String[] BOTTOM_GESTURES = {
            BOTTOM_UP, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_UP_LEFT, BOTTOM_UP_RIGHT,
            BOTTOM_LONG, BOTTOM_DOUBLE, BOTTOM_TRIPLE
    };
    public static final String[] TOP_LEFT_GESTURES = {TOP_LEFT_DOWN, TOP_LEFT_DOUBLE, TOP_LEFT_LONG};
    public static final String[] TOP_RIGHT_GESTURES = {TOP_RIGHT_DOWN, TOP_RIGHT_DOUBLE, TOP_RIGHT_LONG};
    public static final String[] HARDWARE_GESTURES = {
            SHAKE, VOLUME_CHORD, VOLUME_UP_DOUBLE, VOLUME_DOWN_DOUBLE, VOLUME_UP_LONG, VOLUME_DOWN_LONG
    };
    public static final String[] ALL_GESTURES = {
            LEFT_IN, LEFT_UP, LEFT_DOWN, LEFT_DOUBLE, LEFT_TRIPLE, LEFT_LONG, LEFT_IN_UP, LEFT_IN_DOWN,
            RIGHT_IN, RIGHT_UP, RIGHT_DOWN, RIGHT_DOUBLE, RIGHT_TRIPLE, RIGHT_LONG, RIGHT_IN_UP, RIGHT_IN_DOWN,
            BOTTOM_UP, BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM_UP_LEFT, BOTTOM_UP_RIGHT,
            BOTTOM_LONG, BOTTOM_DOUBLE, BOTTOM_TRIPLE,
            TOP_LEFT_DOWN, TOP_LEFT_DOUBLE, TOP_LEFT_LONG,
            TOP_RIGHT_DOWN, TOP_RIGHT_DOUBLE, TOP_RIGHT_LONG,
            SHAKE, VOLUME_CHORD, VOLUME_UP_DOUBLE, VOLUME_DOWN_DOUBLE, VOLUME_UP_LONG, VOLUME_DOWN_LONG
    };

    public static String enabledKey(String gestureKey) {
        return gestureKey + "_enabled";
    }

    public static String appPackageKey(String gestureKey) {
        return gestureKey + "_app_package";
    }

    public static String defaultAction(String gestureKey) {
        switch (gestureKey) {
            case LEFT_IN: return BACK;
            case LEFT_UP: return BACK;
            case LEFT_DOWN: return NOTIFICATIONS;
            case LEFT_DOUBLE: return HOME;
            case LEFT_TRIPLE: return QUICK_SETTINGS;
            case LEFT_LONG: return RECENTS;
            case LEFT_IN_UP: return RECENTS;
            case LEFT_IN_DOWN: return NOTIFICATIONS;
            case RIGHT_IN: return BACK;
            case RIGHT_UP: return RECENTS;
            case RIGHT_DOWN: return QUICK_SETTINGS;
            case RIGHT_DOUBLE: return SCREENSHOT;
            case RIGHT_TRIPLE: return POWER_DIALOG;
            case RIGHT_LONG: return SCREENSHOT;
            case RIGHT_IN_UP: return QUICK_SETTINGS;
            case RIGHT_IN_DOWN: return SCREENSHOT;
            case BOTTOM_UP: return HOME;
            case BOTTOM_LEFT: return BACK;
            case BOTTOM_RIGHT: return RECENTS;
            case BOTTOM_UP_LEFT: return NOTIFICATIONS;
            case BOTTOM_UP_RIGHT: return QUICK_SETTINGS;
            case BOTTOM_LONG: return LOCK;
            case BOTTOM_DOUBLE: return SCREENSHOT;
            case BOTTOM_TRIPLE: return RECENTS;
            case TOP_LEFT_DOWN: return NOTIFICATIONS;
            case TOP_LEFT_DOUBLE: return HOME;
            case TOP_LEFT_LONG: return SCREENSHOT;
            case TOP_RIGHT_DOWN: return QUICK_SETTINGS;
            case TOP_RIGHT_DOUBLE: return RECENTS;
            case TOP_RIGHT_LONG: return LOCK;
            case SHAKE: return SCREENSHOT;
            case VOLUME_CHORD: return SCREENSHOT;
            case VOLUME_UP_DOUBLE: return CAMERA;
            case VOLUME_DOWN_DOUBLE: return SCREENSHOT;
            case VOLUME_UP_LONG: return QUICK_SETTINGS;
            case VOLUME_DOWN_LONG: return NOTIFICATIONS;
            default: return NONE;
        }
    }

    public static boolean defaultEnabled(String gestureKey) {
        return LEFT_IN.equals(gestureKey) || RIGHT_IN.equals(gestureKey);
    }

    public static boolean contains(String[] keys, String target) {
        for (String key : keys) {
            if (key.equals(target)) return true;
        }
        return false;
    }

    public static String gestureForPreferenceKey(String preferenceKey) {
        if (preferenceKey == null) return null;
        for (String gesture : ALL_GESTURES) {
            if (gesture.equals(preferenceKey) || enabledKey(gesture).equals(preferenceKey)) return gesture;
        }
        return null;
    }

    public static void ensureDefaults(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        for (String gesture : ALL_GESTURES) {
            if (!prefs.contains(enabledKey(gesture))) {
                editor.putBoolean(enabledKey(gesture), defaultEnabled(gesture));
            }
            if (!prefs.contains(gesture)) {
                editor.putString(gesture, defaultAction(gesture));
            }
        }
        if (!prefs.contains(EDGE_WIDTH)) editor.putInt(EDGE_WIDTH, 18);
        if (!prefs.contains(HAPTIC)) editor.putBoolean(HAPTIC, true);
        if (!prefs.contains(MASTER_ENABLED)) editor.putBoolean(MASTER_ENABLED, true);
        if (!prefs.contains(LANGUAGE)) editor.putString(LANGUAGE, LANGUAGE_ENGLISH);
        if (prefs.getInt(CONFIG_VERSION, 0) < CURRENT_CONFIG_VERSION) {
            // Version 0.1.0 enabled every edge by default and could steal Pixel's
            // system navigation. Migrate once to a safe, back-gesture-only setup.
            editor.putBoolean(enabledKey(LEFT_IN), true);
            editor.putBoolean(enabledKey(RIGHT_IN), true);
            editor.putBoolean(enabledKey(LEFT_UP), false);
            editor.putBoolean(enabledKey(LEFT_DOWN), false);
            editor.putBoolean(enabledKey(LEFT_DOUBLE), false);
            editor.putBoolean(enabledKey(RIGHT_UP), false);
            editor.putBoolean(enabledKey(RIGHT_DOWN), false);
            editor.putBoolean(enabledKey(RIGHT_DOUBLE), false);
            editor.putBoolean(enabledKey(BOTTOM_UP), false);
            editor.putBoolean(enabledKey(BOTTOM_LONG), false);
            editor.putBoolean(enabledKey(BOTTOM_DOUBLE), false);
            editor.putBoolean(enabledKey(SHAKE), false);
            editor.putBoolean(enabledKey(VOLUME_CHORD), false);
            editor.putInt(EDGE_WIDTH, 18);
            editor.putInt(CONFIG_VERSION, CURRENT_CONFIG_VERSION);
        }
        editor.apply();
    }

    public static void resetToSafeDefaults(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        for (String gesture : ALL_GESTURES) {
            editor.putBoolean(enabledKey(gesture), defaultEnabled(gesture));
            editor.putString(gesture, defaultAction(gesture));
            editor.remove(appPackageKey(gesture));
        }
        editor.putInt(EDGE_WIDTH, 18);
        editor.putBoolean(HAPTIC, true);
        editor.putBoolean(MASTER_ENABLED, true);
        editor.putInt(CONFIG_VERSION, CURRENT_CONFIG_VERSION);
        editor.apply();
    }
}
