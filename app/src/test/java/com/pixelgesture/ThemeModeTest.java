package com.pixelgesture;
import org.junit.Test;
import static org.junit.Assert.*;
public final class ThemeModeTest {
    @Test public void unknownAndMissingPreferencesFollowSystem() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.normalize(null));
        assertEquals(ThemeMode.SYSTEM, ThemeMode.normalize("old-or-invalid"));
        assertEquals(0x23, ThemeMode.apply(ThemeMode.SYSTEM, 0x13, 0x22));
    }
    @Test public void forcedLightAndDarkPreserveOtherUiBits() {
        assertEquals(0x17, ThemeMode.apply(ThemeMode.LIGHT, 0x27, 0x20));
        assertEquals(0x27, ThemeMode.apply(ThemeMode.DARK, 0x17, 0x10));
    }
    @Test public void returnToSystemUsesDeviceRatherThanForcedActivity() {
        assertEquals(0x12, ThemeMode.apply(ThemeMode.SYSTEM, 0x22, 0x11));
        assertEquals(0x22, ThemeMode.apply(ThemeMode.SYSTEM, 0x12, 0x21));
    }
}
