package com.pixelgesture;

import android.content.SharedPreferences;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/** A bounded, allowlisted backup; parsing never changes live preferences. */
final class ConfigBackup {
    private static final String FORMAT = "pixelgesture_backup";
    private static final int MAX_BYTES = 64 * 1024;

    static void write(Map<String, ?> preferences, OutputStream output) throws IOException {
        Properties data = new Properties();
        data.setProperty(FORMAT, "1");
        data.setProperty(AppConfig.CONFIG_VERSION, String.valueOf(AppConfig.CURRENT_CONFIG_VERSION));
        data.setProperty(AppConfig.EDGE_WIDTH, String.valueOf(preferences.get(AppConfig.EDGE_WIDTH)));
        data.setProperty(AppConfig.HAPTIC, String.valueOf(preferences.get(AppConfig.HAPTIC)));
        data.setProperty(AppConfig.LANGUAGE, String.valueOf(preferences.get(AppConfig.LANGUAGE)));
        for (String gesture : AppConfig.ALL_GESTURES) {
            data.setProperty(gesture, String.valueOf(preferences.get(gesture)));
            data.setProperty(AppConfig.enabledKey(gesture), String.valueOf(preferences.get(AppConfig.enabledKey(gesture))));
            Object pkg = preferences.get(AppConfig.appPackageKey(gesture));
            if (pkg != null) data.setProperty(AppConfig.appPackageKey(gesture), String.valueOf(pkg));
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        data.store(bytes, "Pixel Gesture settings. Contains selected app package names.");
        byte[] content = bytes.toByteArray();
        read(new ByteArrayInputStream(content));
        output.write(content);
    }

    static Map<String, Object> read(InputStream input) throws IOException {
        if (input == null) throw new IOException("No input stream");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (bytes.size() + count > MAX_BYTES) throw new IOException("Backup too large");
            bytes.write(buffer, 0, count);
        }
        Properties data = new Properties() {
            @Override public synchronized Object put(Object key, Object value) {
                if (containsKey(key)) throw new IllegalArgumentException("Duplicate key");
                return super.put(key, value);
            }
        };
        try {
            data.load(new ByteArrayInputStream(bytes.toByteArray()));
            if (!"1".equals(data.remove(FORMAT))
                    || !String.valueOf(AppConfig.CURRENT_CONFIG_VERSION).equals(data.remove(AppConfig.CONFIG_VERSION))) {
                throw new IllegalArgumentException("Unsupported backup version");
            }
            Map<String, Object> values = new LinkedHashMap<>();
            int width = Integer.parseInt(required(data, AppConfig.EDGE_WIDTH));
            if (width < 12 || width > 40) throw new IllegalArgumentException("Invalid edge width");
            values.put(AppConfig.EDGE_WIDTH, width);
            values.put(AppConfig.HAPTIC, bool(data, AppConfig.HAPTIC));
            String language = required(data, AppConfig.LANGUAGE);
            if (!AppConfig.LANGUAGE_ENGLISH.equals(language) && !AppConfig.LANGUAGE_CHINESE.equals(language)) {
                throw new IllegalArgumentException("Unknown language");
            }
            values.put(AppConfig.LANGUAGE, language);
            for (String gesture : AppConfig.ALL_GESTURES) {
                String action = required(data, gesture);
                if (!AppConfig.contains(AppConfig.ACTION_KEYS, action)) throw new IllegalArgumentException("Unknown action");
                values.put(gesture, action);
                values.put(AppConfig.enabledKey(gesture), bool(data, AppConfig.enabledKey(gesture)));
                String key = AppConfig.appPackageKey(gesture);
                String pkg = (String) data.remove(key);
                if (pkg != null) {
                    if (pkg.length() > 255 || !pkg.matches("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)+")) {
                        throw new IllegalArgumentException("Invalid app package");
                    }
                    values.put(key, pkg);
                }
            }
            if (!data.isEmpty()) throw new IllegalArgumentException("Unknown settings");
            // Restoring bindings must never activate overlays or hardware gestures.
            values.put(AppConfig.MASTER_ENABLED, false);
            values.put(AppConfig.CONFIG_VERSION, AppConfig.CURRENT_CONFIG_VERSION);
            return values;
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid Pixel Gesture backup", e);
        }
    }

    static void restore(SharedPreferences preferences, Map<String, Object> values) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String gesture : AppConfig.ALL_GESTURES) editor.remove(AppConfig.appPackageKey(gesture));
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) value);
            else if (value instanceof Integer) editor.putInt(entry.getKey(), (Integer) value);
            else editor.putString(entry.getKey(), (String) value);
        }
        editor.apply();
    }

    private static String required(Properties data, String key) {
        String value = (String) data.remove(key);
        if (value == null) throw new IllegalArgumentException("Missing setting");
        return value;
    }

    private static boolean bool(Properties data, String key) {
        String value = required(data, key);
        if (!"true".equals(value) && !"false".equals(value)) throw new IllegalArgumentException("Invalid boolean");
        return Boolean.parseBoolean(value);
    }
}
