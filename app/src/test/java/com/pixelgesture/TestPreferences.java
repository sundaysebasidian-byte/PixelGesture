package com.pixelgesture;

import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** In-memory preference contract for configuration tests; no Android lifecycle simulation. */
final class TestPreferences implements SharedPreferences {
    private final Map<String, Object> values = new HashMap<>();
    @Override public Map<String, ?> getAll() { return new HashMap<>(values); }
    @Override public boolean contains(String key) { return values.containsKey(key); }
    @Override public String getString(String key, String fallback) { return (String) values.getOrDefault(key, fallback); }
    @SuppressWarnings("unchecked")
    @Override public Set<String> getStringSet(String key, Set<String> fallback) { return (Set<String>) values.getOrDefault(key, fallback); }
    @Override public int getInt(String key, int fallback) { return (Integer) values.getOrDefault(key, fallback); }
    @Override public long getLong(String key, long fallback) { return (Long) values.getOrDefault(key, fallback); }
    @Override public float getFloat(String key, float fallback) { return (Float) values.getOrDefault(key, fallback); }
    @Override public boolean getBoolean(String key, boolean fallback) { return (Boolean) values.getOrDefault(key, fallback); }
    @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }
    @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) { }
    @Override public Editor edit() {
        return new Editor() {
            private final Map<String, Object> changes = new HashMap<>();
            private boolean clear;
            private Editor put(String key, Object value) { changes.put(key, value); return this; }
            @Override public Editor putString(String key, String value) { return put(key, value); }
            @Override public Editor putStringSet(String key, Set<String> value) { return put(key, value); }
            @Override public Editor putInt(String key, int value) { return put(key, value); }
            @Override public Editor putLong(String key, long value) { return put(key, value); }
            @Override public Editor putFloat(String key, float value) { return put(key, value); }
            @Override public Editor putBoolean(String key, boolean value) { return put(key, value); }
            @Override public Editor remove(String key) { return put(key, null); }
            @Override public Editor clear() { clear = true; return this; }
            @Override public boolean commit() { apply(); return true; }
            @Override public void apply() {
                if (clear) values.clear();
                for (Map.Entry<String, Object> entry : changes.entrySet()) {
                    if (entry.getValue() == null) values.remove(entry.getKey());
                    else values.put(entry.getKey(), entry.getValue());
                }
            }
        };
    }
}
