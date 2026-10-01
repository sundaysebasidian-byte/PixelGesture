package com.pixelgesture;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ConfigBackupTest {
    private TestPreferences defaults() {
        TestPreferences prefs = new TestPreferences();
        AppConfig.ensureDefaults(prefs);
        return prefs;
    }
    private String backup(TestPreferences prefs) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ConfigBackup.write(prefs.getAll(), out);
        return out.toString("ISO-8859-1");
    }
    private Map<String, Object> read(String text) throws IOException {
        return ConfigBackup.read(new ByteArrayInputStream(text.getBytes(StandardCharsets.ISO_8859_1)));
    }
    private void rejects(String text) {
        assertThrows(IOException.class, () -> read(text));
    }
    @Test public void roundTripKeepsAllBindingsAndPausesWithoutRemigration() throws IOException {
        TestPreferences source = defaults();
        source.edit().putString(AppConfig.RIGHT_DOUBLE, AppConfig.OPEN_APP)
                .putString(AppConfig.appPackageKey(AppConfig.RIGHT_DOUBLE), "com.example.camera")
                .putBoolean(AppConfig.enabledKey(AppConfig.RIGHT_DOUBLE), true)
                .putInt(AppConfig.EDGE_WIDTH, 32).putString(AppConfig.LANGUAGE, "zh").apply();
        TestPreferences target = defaults();
        target.edit().putString(AppConfig.appPackageKey(AppConfig.LEFT_DOUBLE), "com.old.app").apply();
        ConfigBackup.restore(target, read(backup(source)));
        AppConfig.ensureDefaults(target);
        assertFalse(target.getBoolean(AppConfig.MASTER_ENABLED, true));
        assertEquals(32, target.getInt(AppConfig.EDGE_WIDTH, 0));
        assertEquals("zh", target.getString(AppConfig.LANGUAGE, ""));
        assertFalse(target.contains(AppConfig.appPackageKey(AppConfig.LEFT_DOUBLE)));
        for (String gesture : AppConfig.ALL_GESTURES) {
            assertEquals(source.getString(gesture, ""), target.getString(gesture, ""));
            assertEquals(source.getBoolean(AppConfig.enabledKey(gesture), false), target.getBoolean(AppConfig.enabledKey(gesture), false));
        }
        assertEquals("com.example.camera", target.getString(AppConfig.appPackageKey(AppConfig.RIGHT_DOUBLE), ""));
    }
    @Test public void parseDoesNotChangeLivePreferences() throws IOException {
        TestPreferences prefs = defaults();
        Map<String, ?> before = prefs.getAll();
        read(backup(prefs));
        assertEquals(before, prefs.getAll());
        rejects(backup(prefs).replace("edge_width=18", "edge_width=oops"));
        assertEquals(before, prefs.getAll());
    }
    @Test public void unsupportedVersionsAreRejected() throws IOException {
        String valid = backup(defaults());
        rejects(valid.replace("pixelgesture_backup=1", "pixelgesture_backup=2"));
        rejects(valid.replace("config_version=2", "config_version=3"));
    }
    @Test public void unknownKeysAndActivationFlagsAreRejected() throws IOException {
        String valid = backup(defaults());
        rejects(valid + "\nunknown=value\n");
        rejects(valid + "\nmaster_enabled=true\n");
    }
    @Test public void duplicateMissingAndTruncatedSettingsAreRejected() throws IOException {
        String valid = backup(defaults());
        rejects(valid + "\nedge_width=20\n");
        rejects(valid.replace("left_in=back", ""));
        rejects(valid.substring(0, valid.length() / 2));
        rejects("");
    }
    @Test public void invalidActionsAndBooleansAreRejected() throws IOException {
        String valid = backup(defaults());
        rejects(valid.replace("left_in=back", "left_in=remote"));
        rejects(valid.replace("left_in_enabled=true", "left_in_enabled=yes"));
    }
    @Test public void widthBoundsAndLanguageAreValidated() throws IOException {
        String valid = backup(defaults());
        assertEquals(12, read(valid.replace("edge_width=18", "edge_width=12")).get(AppConfig.EDGE_WIDTH));
        assertEquals(40, read(valid.replace("edge_width=18", "edge_width=40")).get(AppConfig.EDGE_WIDTH));
        rejects(valid.replace("edge_width=18", "edge_width=11"));
        rejects(valid.replace("edge_width=18", "edge_width=41"));
        rejects(valid.replace("language=en", "language=fr"));
    }
    @Test public void malformedPackageAndUnicodeEscapesAreRejected() throws IOException {
        String valid = backup(defaults());
        rejects(valid + "\nleft_in_app_package=../../other\n");
        rejects(valid + "\nleft_in_app_package=com." + String.join("", java.util.Collections.nCopies(256, "a")) + "\n");
        rejects(valid + "\nleft_in_app_package=\\uXXXX\n");
    }
    @Test public void oversizedAndNullInputsAreRejected() {
        assertThrows(IOException.class, () -> ConfigBackup.read(new ByteArrayInputStream(new byte[65537])));
        assertThrows(IOException.class, () -> ConfigBackup.read(null));
    }
    @Test public void invalidExportDoesNotWritePartialBackup() {
        TestPreferences source = defaults();
        source.edit().putInt(AppConfig.EDGE_WIDTH, 99).apply();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertThrows(IOException.class, () -> ConfigBackup.write(source.getAll(), out));
        assertEquals(0, out.size());
    }
    @Test public void existingVersionTwoConfigurationIsNotOverwrittenByDefaults() {
        TestPreferences prefs = defaults();
        prefs.edit().putBoolean(AppConfig.enabledKey(AppConfig.SHAKE), true)
                .putInt(AppConfig.EDGE_WIDTH, 28).putBoolean(AppConfig.MASTER_ENABLED, false).apply();
        AppConfig.ensureDefaults(prefs);
        assertTrue(prefs.getBoolean(AppConfig.enabledKey(AppConfig.SHAKE), false));
        assertEquals(28, prefs.getInt(AppConfig.EDGE_WIDTH, 0));
        assertFalse(prefs.getBoolean(AppConfig.MASTER_ENABLED, true));
    }
    @Test public void legacyMigrationRunsOnceAndUsesSafeWidth() {
        TestPreferences prefs = new TestPreferences();
        prefs.edit().putBoolean(AppConfig.enabledKey(AppConfig.SHAKE), true).putInt(AppConfig.EDGE_WIDTH, 40).apply();
        AppConfig.ensureDefaults(prefs);
        assertFalse(prefs.getBoolean(AppConfig.enabledKey(AppConfig.SHAKE), true));
        assertEquals(18, prefs.getInt(AppConfig.EDGE_WIDTH, 0));
        prefs.edit().putInt(AppConfig.EDGE_WIDTH, 24).apply();
        AppConfig.ensureDefaults(prefs);
        assertEquals(24, prefs.getInt(AppConfig.EDGE_WIDTH, 0));
    }
}
