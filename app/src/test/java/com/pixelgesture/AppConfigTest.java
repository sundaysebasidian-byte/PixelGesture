package com.pixelgesture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public final class AppConfigTest {
    @Test
    public void gestureCatalogHasThirtySixUniqueEntries() {
        Set<String> unique = new HashSet<>(Arrays.asList(AppConfig.ALL_GESTURES));
        assertEquals(36, AppConfig.ALL_GESTURES.length);
        assertEquals(AppConfig.ALL_GESTURES.length, unique.size());
    }

    @Test
    public void actionCatalogHasTwelveUniqueEntries() {
        Set<String> unique = new HashSet<>(Arrays.asList(AppConfig.ACTION_KEYS));
        assertEquals(12, AppConfig.ACTION_KEYS.length);
        assertEquals(AppConfig.ACTION_KEYS.length, unique.size());
    }

    @Test
    public void everyGestureUsesARegisteredDefaultAction() {
        Set<String> actions = new HashSet<>(Arrays.asList(AppConfig.ACTION_KEYS));
        for (String gesture : AppConfig.ALL_GESTURES) {
            assertTrue("Unknown action for " + gesture, actions.contains(AppConfig.defaultAction(gesture)));
        }
    }

    @Test
    public void onlySafeBackGesturesAreEnabledByDefault() {
        for (String gesture : AppConfig.ALL_GESTURES) {
            boolean expected = AppConfig.LEFT_IN.equals(gesture) || AppConfig.RIGHT_IN.equals(gesture);
            assertEquals("Unexpected default for " + gesture, expected, AppConfig.defaultEnabled(gesture));
        }
        assertEquals(AppConfig.BACK, AppConfig.defaultAction(AppConfig.LEFT_IN));
        assertEquals(AppConfig.BACK, AppConfig.defaultAction(AppConfig.RIGHT_IN));
    }

    @Test
    public void preferenceKeysMapBackToTheirGesture() {
        for (String gesture : AppConfig.ALL_GESTURES) {
            assertEquals(gesture, AppConfig.gestureForPreferenceKey(gesture));
            assertEquals(gesture, AppConfig.gestureForPreferenceKey(AppConfig.enabledKey(gesture)));
            assertFalse(AppConfig.appPackageKey(gesture).isEmpty());
        }
        assertNull(AppConfig.gestureForPreferenceKey(null));
        assertNull(AppConfig.gestureForPreferenceKey("unknown"));
    }
}
