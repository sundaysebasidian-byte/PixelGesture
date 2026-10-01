package com.pixelgesture;

/** Timing state independent of Android event delivery and delayed callbacks. */
final class VolumeGestureState {
    private static final long DOUBLE_MS = 360L;
    private static final long CHORD_MS = 500L;
    private static final long TAP_MAX_MS = 650L;
    private final boolean[] pressed = new boolean[2];
    private final boolean[] longTriggered = new boolean[2];
    private final long[] pressedAt = new long[2];
    private final long[] lastTapAt = {-1L, -1L};
    private boolean chordTriggered;

    boolean isPressed(boolean up) {
        return pressed[up ? 0 : 1];
    }

    String press(boolean up, long now, boolean chordEnabled) {
        int key = up ? 0 : 1;
        if (pressed[key]) return null;
        pressed[key] = true;
        pressedAt[key] = now;
        longTriggered[key] = false;
        if (chordEnabled && pressed[0] && pressed[1] && !chordTriggered
                && !longTriggered[1 - key]
                && Math.abs(pressedAt[0] - pressedAt[1]) <= CHORD_MS) {
            chordTriggered = true;
            clearTaps();
            return AppConfig.VOLUME_CHORD;
        }
        return null;
    }

    String longPress(boolean up) {
        int key = up ? 0 : 1;
        if (!pressed[key] || chordTriggered || longTriggered[key]) return null;
        longTriggered[key] = true;
        lastTapAt[key] = -1L;
        return up ? AppConfig.VOLUME_UP_LONG : AppConfig.VOLUME_DOWN_LONG;
    }

    String release(boolean up, long now, boolean doubleEnabled) {
        int key = up ? 0 : 1;
        if (!pressed[key]) return null;
        pressed[key] = false;
        String result = null;
        long held = now - pressedAt[key];
        if (!chordTriggered && !longTriggered[key] && doubleEnabled
                && held >= 0L && held < TAP_MAX_MS) {
            if (lastTapAt[key] >= 0L && now >= lastTapAt[key]
                    && now - lastTapAt[key] <= DOUBLE_MS) {
                lastTapAt[key] = -1L;
                result = up ? AppConfig.VOLUME_UP_DOUBLE : AppConfig.VOLUME_DOWN_DOUBLE;
            } else {
                lastTapAt[key] = now;
            }
        } else {
            lastTapAt[key] = -1L;
        }
        if (!pressed[0] && !pressed[1]) chordTriggered = false;
        return result;
    }

    void reset() {
        pressed[0] = pressed[1] = false;
        longTriggered[0] = longTriggered[1] = false;
        chordTriggered = false;
        clearTaps();
    }

    private void clearTaps() {
        lastTapAt[0] = lastTapAt[1] = -1L;
    }
}
