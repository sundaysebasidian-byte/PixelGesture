package com.pixelgesture;

import org.junit.Test;
import static org.junit.Assert.*;

public final class VolumeGestureStateTest {
    private final VolumeGestureState state = new VolumeGestureState();

    private String tap(boolean up, long at) {
        state.press(up, at, false);
        return state.release(up, at + 10L, true);
    }

    @Test public void firstTapNearBootIsNotADouble() {
        assertNull(tap(true, 0L));
        assertEquals(AppConfig.VOLUME_UP_DOUBLE, tap(true, 100L));
        assertNull(tap(true, 200L));
    }
    @Test public void doubleWindowIncludesBoundaryButNotExpiredTap() {
        assertNull(tap(false, 1000L));
        assertEquals(AppConfig.VOLUME_DOWN_DOUBLE, tap(false, 1360L));
        assertNull(tap(false, 2000L));
        assertNull(tap(false, 2361L));
    }
    @Test public void releasesWithoutDownAndDuplicateReleasesAreIgnored() {
        assertNull(state.release(true, 100L, true));
        assertNull(tap(true, 1000L));
        assertNull(state.release(true, 1020L, true));
        assertEquals(AppConfig.VOLUME_UP_DOUBLE, tap(true, 1100L));
    }
    @Test public void oppositeKeysDoNotCombineIntoDouble() {
        assertNull(tap(true, 1000L));
        assertNull(tap(false, 1100L));
    }
    @Test public void resetCancelsHeldKeysDelayedCallbacksAndTapHistory() {
        tap(true, 1000L);
        state.press(false, 1100L, true);
        state.reset();
        assertNull(state.longPress(false));
        assertNull(state.release(false, 1110L, true));
        assertNull(tap(true, 1200L));
    }
    @Test public void longPressFiresOnceAndSuppressesDouble() {
        tap(true, 1000L);
        state.press(true, 1100L, false);
        assertEquals(AppConfig.VOLUME_UP_LONG, state.longPress(true));
        assertNull(state.longPress(true));
        assertNull(state.release(true, 1800L, true));
        assertNull(tap(true, 1900L));
    }
    @Test public void longHoldIsNotATapEvenWhenLongActionIsDisabled() {
        state.press(true, 1000L, false);
        assertNull(state.release(true, 1650L, true));
        assertNull(tap(true, 1700L));
    }
    @Test public void chordSuppressesBothLongPressAndDoubleActions() {
        state.press(true, 1000L, true);
        assertEquals(AppConfig.VOLUME_CHORD, state.press(false, 1500L, true));
        assertNull(state.longPress(true));
        assertNull(state.longPress(false));
        assertNull(state.release(true, 1510L, true));
        assertNull(state.release(false, 1520L, true));
        assertNull(tap(false, 1600L));
    }
    @Test public void chordOutsideWindowOrAfterLongPressDoesNotFire() {
        state.press(true, 1000L, true);
        assertNull(state.press(false, 1501L, true));
        state.reset();
        state.press(true, 2000L, true);
        state.longPress(true);
        assertNull(state.press(false, 2100L, true));
    }
    @Test public void repeatDownDoesNotRestartHoldTiming() {
        state.press(true, 1000L, false);
        assertNull(state.press(true, 1500L, false));
        assertNull(state.release(true, 1660L, true));
        assertNull(tap(true, 1700L));
    }
    @Test public void disabledDoubleDoesNotSeedLaterDouble() {
        state.press(true, 1000L, false);
        assertNull(state.release(true, 1010L, false));
        assertNull(tap(true, 1100L));
    }
    @Test public void releasedKeyCannotTriggerDelayedLongPress() {
        tap(true, 1000L);
        assertNull(state.longPress(true));
    }
}
