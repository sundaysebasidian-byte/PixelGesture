package com.pixelgesture;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/** Global gesture engine. It uses a narrow accessibility overlay instead of reading app content. */
public class GestureAccessibilityService extends AccessibilityService implements SensorEventListener {
    private static final long DOUBLE_TAP_MS = 320L;
    private static final long LONG_PRESS_MS = 550L;
    private static final long VOLUME_LONG_MS = 650L;
    private static final long SHAKE_COOLDOWN_MS = 1000L;
    private static final float SWIPE_MIN_DP = 56f;

    private android.content.SharedPreferences prefs;
    private WindowManager windowManager;
    private final Map<String, EdgeGestureView> overlays = new HashMap<>();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean shakeRegistered;
    private boolean serviceConnected;
    private long lastShakeAt;
    private float lastAcceleration;
    private final VolumeGestureState volumeState = new VolumeGestureState();
    private final Handler keyHandler = new Handler(Looper.getMainLooper());
    private final Runnable volumeUpLongRunnable = () -> triggerVolumeLong(true);
    private final Runnable volumeDownLongRunnable = () -> triggerVolumeLong(false);

    private void triggerVolumeLong(boolean up) {
        String key = up ? AppConfig.VOLUME_UP_LONG : AppConfig.VOLUME_DOWN_LONG;
        if (isEnabled(key)) {
            String gesture = volumeState.longPress(up);
            if (gesture != null) triggerGesture(gesture);
        }
    }
    private android.content.SharedPreferences.OnSharedPreferenceChangeListener preferenceListener;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        releaseServiceResources();
        serviceConnected = true;
        prefs = getSharedPreferences(AppConfig.PREFS, MODE_PRIVATE);
        AppConfig.ensureDefaults(prefs);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        preferenceListener = (sharedPreferences, key) -> onPreferenceChanged(key);
        prefs.registerOnSharedPreferenceChangeListener(preferenceListener);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        refreshOverlays(true);
        updateShakeSensor();
    }

    private void onPreferenceChanged(String key) {
        if (!serviceConnected) return;
        if (AppConfig.MASTER_ENABLED.equals(key)) {
            refreshOverlays(false);
            updateShakeSensor();
            if (!prefs.getBoolean(AppConfig.MASTER_ENABLED, true)) onInterrupt();
            return;
        }
        if (AppConfig.EDGE_WIDTH.equals(key)) {
            refreshOverlays(true);
            return;
        }
        String gesture = AppConfig.gestureForPreferenceKey(key);
        if (gesture == null) return;
        if (AppConfig.contains(AppConfig.HARDWARE_GESTURES, gesture)) onInterrupt();
        if (AppConfig.SHAKE.equals(gesture)) updateShakeSensor();
        if (AppConfig.contains(AppConfig.LEFT_GESTURES, gesture)
                || AppConfig.contains(AppConfig.RIGHT_GESTURES, gesture)
                || AppConfig.contains(AppConfig.BOTTOM_GESTURES, gesture)
                || AppConfig.contains(AppConfig.TOP_LEFT_GESTURES, gesture)
                || AppConfig.contains(AppConfig.TOP_RIGHT_GESTURES, gesture)) {
            refreshOverlays(false);
        }
    }

    private void refreshOverlays(boolean forceRecreate) {
        if (!serviceConnected || windowManager == null || prefs == null) return;
        if (forceRecreate) removeAllOverlays();
        boolean master = prefs.getBoolean(AppConfig.MASTER_ENABLED, true);
        syncOverlay("left", EdgeGestureView.SIDE_LEFT, master && anyEnabled(AppConfig.LEFT_GESTURES));
        syncOverlay("right", EdgeGestureView.SIDE_RIGHT, master && anyEnabled(AppConfig.RIGHT_GESTURES));
        syncOverlay("bottom", EdgeGestureView.SIDE_BOTTOM, master && anyEnabled(AppConfig.BOTTOM_GESTURES));
        syncOverlay("top_left", EdgeGestureView.SIDE_TOP_LEFT, master && anyEnabled(AppConfig.TOP_LEFT_GESTURES));
        syncOverlay("top_right", EdgeGestureView.SIDE_TOP_RIGHT, master && anyEnabled(AppConfig.TOP_RIGHT_GESTURES));
    }

    private void syncOverlay(String name, int side, boolean needed) {
        EdgeGestureView current = overlays.get(name);
        if (needed && current == null) {
            addOverlay(name, side);
        } else if (!needed && current != null) {
            try { windowManager.removeView(current); } catch (Exception ignored) { }
            overlays.remove(name);
        }
    }

    private void addOverlay(String name, int side) {
        EdgeGestureView view = new EdgeGestureView(this, side);
        int width = dp(Math.max(12, Math.min(40, prefs.getInt(AppConfig.EDGE_WIDTH, 18))));
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        params.format = PixelFormat.TRANSLUCENT;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int topInset = 0;
        int bottomInset = 0;
        int leftInset = 0;
        int rightInset = 0;
        try {
            WindowMetrics metrics = windowManager.getCurrentWindowMetrics();
            Rect bounds = metrics.getBounds();
            Insets bars = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            screenHeight = bounds.height();
            screenWidth = bounds.width();
            topInset = bars.top;
            bottomInset = bars.bottom;
            leftInset = bars.left;
            rightInset = bars.right;
        } catch (Exception ignored) { }
        int availableWidth = OverlayGeometry.available(screenWidth, leftInset, rightInset);
        int availableHeight = OverlayGeometry.available(screenHeight, topInset, bottomInset);
        if (availableWidth == 0 || availableHeight <= dp(24)) return;
        if (side == EdgeGestureView.SIDE_BOTTOM) {
            params.gravity = Gravity.BOTTOM | Gravity.LEFT;
            params.width = availableWidth;
            params.height = Math.min(width, OverlayGeometry.sideHeight(screenHeight,
                    topInset, bottomInset, Math.max(0, dp(8) - bottomInset)));
            params.x = leftInset;
            params.y = Math.max(bottomInset, dp(8));
        } else if (side == EdgeGestureView.SIDE_TOP_LEFT || side == EdgeGestureView.SIDE_TOP_RIGHT) {
            params.gravity = (side == EdgeGestureView.SIDE_TOP_LEFT ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP;
            params.width = Math.min(dp(88), availableWidth);
            params.height = Math.min(width, OverlayGeometry.sideHeight(screenHeight,
                    topInset, bottomInset, dp(4)));
            params.x = side == EdgeGestureView.SIDE_TOP_LEFT ? leftInset : rightInset;
            params.y = topInset + dp(4);
        } else {
            params.gravity = (side == EdgeGestureView.SIDE_LEFT ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP;
            params.width = Math.min(width, availableWidth);
            params.height = OverlayGeometry.sideHeight(screenHeight, topInset, bottomInset, dp(24));
            params.x = side == EdgeGestureView.SIDE_LEFT ? leftInset : rightInset;
            params.y = topInset + dp(12);
        }
        try {
            windowManager.addView(view, params);
            overlays.put(name, view);
        } catch (WindowManager.BadTokenException | IllegalStateException ignored) {
            // The system can tear down an accessibility window during a user switch.
        }
    }

    private void removeAllOverlays() {
        for (EdgeGestureView view : overlays.values()) {
            try { windowManager.removeView(view); } catch (Exception ignored) { }
        }
        overlays.clear();
    }

    private boolean isEnabled(String key) {
        return prefs != null && prefs.getBoolean(AppConfig.enabledKey(key), false)
                && !AppConfig.NONE.equals(prefs.getString(key, AppConfig.NONE));
    }

    private boolean anyEnabled(String... keys) {
        for (String key : keys) {
            if (isEnabled(key)) return true;
        }
        return false;
    }

    private void updateShakeSensor() {
        if (sensorManager == null || accelerometer == null) return;
        boolean shouldRegister = serviceConnected && prefs != null
                && prefs.getBoolean(AppConfig.MASTER_ENABLED, true) && isEnabled(AppConfig.SHAKE);
        if (shouldRegister && !shakeRegistered) {
            lastAcceleration = 9.8f;
            // UI-rate sampling is responsive enough for shake detection and uses less power
            // than game-rate sampling while the accessibility service is active.
            shakeRegistered = sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else if (!shouldRegister && shakeRegistered) {
            sensorManager.unregisterListener(this);
            shakeRegistered = false;
        }
    }

    public void triggerGesture(String key) {
        if (!serviceConnected || prefs == null
                || !prefs.getBoolean(AppConfig.MASTER_ENABLED, true) || !isEnabled(key)) return;
        performAction(key, prefs.getString(key, AppConfig.NONE));
    }

    private void performAction(String gestureKey, String action) {
        boolean performed = false;
        try {
            switch (action) {
                case AppConfig.BACK:
                    performed = performGlobalAction(GLOBAL_ACTION_BACK); break;
                case AppConfig.HOME:
                    performed = performGlobalAction(GLOBAL_ACTION_HOME); break;
                case AppConfig.RECENTS:
                    performed = performGlobalAction(GLOBAL_ACTION_RECENTS); break;
                case AppConfig.NOTIFICATIONS:
                    performed = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS); break;
                case AppConfig.QUICK_SETTINGS:
                    performed = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS); break;
                case AppConfig.SCREENSHOT:
                    performed = performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT);
                    break;
                case AppConfig.LOCK:
                    performed = performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                    break;
                case AppConfig.POWER_DIALOG:
                    performed = performGlobalAction(GLOBAL_ACTION_POWER_DIALOG); break;
                case AppConfig.CAMERA:
                    Intent camera = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                    camera.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(camera);
                    performed = true;
                    break;
                case AppConfig.OPEN_APP:
                    String packageName = prefs.getString(AppConfig.appPackageKey(gestureKey), "");
                    Intent launchIntent = packageName == null || packageName.isEmpty()
                            ? null : getPackageManager().getLaunchIntentForPackage(packageName);
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(launchIntent);
                        performed = true;
                    } else {
                        Toast.makeText(this, LanguageManager.getString(this, R.string.choose_app_first), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case AppConfig.SETTINGS:
                    Intent settings = new Intent(Settings.ACTION_SETTINGS);
                    settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settings);
                    performed = true;
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {
            performed = false;
        }
        if (performed && prefs.getBoolean(AppConfig.HAPTIC, true)) vibrateBriefly();
    }

    @SuppressWarnings("deprecation")
    private void vibrateBriefly() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) getSystemService(VIBRATOR_MANAGER_SERVICE);
            vibrator = manager == null ? null : manager.getDefaultVibrator();
        } else {
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        }
        if (vibrator == null || !vibrator.hasVibrator()) return;
        vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        if (!serviceConnected || prefs == null || !prefs.getBoolean(AppConfig.MASTER_ENABLED, true)
                || !anyEnabled(AppConfig.VOLUME_CHORD, AppConfig.VOLUME_UP_DOUBLE,
                AppConfig.VOLUME_DOWN_DOUBLE, AppConfig.VOLUME_UP_LONG, AppConfig.VOLUME_DOWN_LONG)) return false;
        int code = event.getKeyCode();
        if (code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN) return false;
        boolean interceptKey = code == KeyEvent.KEYCODE_VOLUME_UP
                ? anyEnabled(AppConfig.VOLUME_UP_DOUBLE, AppConfig.VOLUME_UP_LONG)
                : anyEnabled(AppConfig.VOLUME_DOWN_DOUBLE, AppConfig.VOLUME_DOWN_LONG);
        boolean up = code == KeyEvent.KEYCODE_VOLUME_UP;
        Runnable longRunnable = up ? volumeUpLongRunnable : volumeDownLongRunnable;
        long now = SystemClock.uptimeMillis();
        if (event.isCanceled()) {
            onInterrupt();
        } else if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0
                && !volumeState.isPressed(up)) {
            String gesture = volumeState.press(up, now, isEnabled(AppConfig.VOLUME_CHORD));
            if (gesture != null) {
                keyHandler.removeCallbacks(volumeUpLongRunnable);
                keyHandler.removeCallbacks(volumeDownLongRunnable);
                triggerGesture(gesture);
            } else if (isEnabled(up ? AppConfig.VOLUME_UP_LONG : AppConfig.VOLUME_DOWN_LONG)) {
                keyHandler.postDelayed(longRunnable, VOLUME_LONG_MS);
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            keyHandler.removeCallbacks(longRunnable);
            String gesture = volumeState.release(up, now,
                    isEnabled(up ? AppConfig.VOLUME_UP_DOUBLE : AppConfig.VOLUME_DOWN_DOUBLE));
            if (gesture != null) triggerGesture(gesture);
        }
        return interceptKey;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        refreshOverlays(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!serviceConnected || event.sensor.getType() != Sensor.TYPE_ACCELEROMETER || !isEnabled(AppConfig.SHAKE)) return;
        float x = event.values[0], y = event.values[1], z = event.values[2];
        float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
        float delta = Math.abs(magnitude - lastAcceleration);
        lastAcceleration = magnitude;
        long now = SystemClock.uptimeMillis();
        if (delta > 8.5f && now - lastShakeAt > SHAKE_COOLDOWN_MS) {
            lastShakeAt = now;
            triggerGesture(AppConfig.SHAKE);
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) { }

    @Override
    public void onInterrupt() {
        keyHandler.removeCallbacks(volumeUpLongRunnable);
        keyHandler.removeCallbacks(volumeDownLongRunnable);
        volumeState.reset();
        for (EdgeGestureView view : overlays.values()) view.cancelPendingGesture();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        releaseServiceResources();
        return super.onUnbind(intent);
    }

    private void releaseServiceResources() {
        serviceConnected = false;
        if (prefs != null && preferenceListener != null) prefs.unregisterOnSharedPreferenceChangeListener(preferenceListener);
        preferenceListener = null;
        if (sensorManager != null && shakeRegistered) sensorManager.unregisterListener(this);
        shakeRegistered = false;
        onInterrupt();
        removeAllOverlays();
        windowManager = null;
        sensorManager = null;
        accelerometer = null;
        prefs = null;
    }

    @Override
    public void onDestroy() {
        releaseServiceResources();
        super.onDestroy();
    }

    public static class EdgeGestureView extends View {
        public static final int SIDE_LEFT = 0;
        public static final int SIDE_RIGHT = 1;
        public static final int SIDE_BOTTOM = 2;
        public static final int SIDE_TOP_LEFT = 3;
        public static final int SIDE_TOP_RIGHT = 4;

        private final GestureAccessibilityService service;
        private final int side;
        private float downX;
        private float downY;
        private long downAt;
        private long lastTapAt;
        private int tapCount;
        private boolean longTriggered;
        private boolean touchActive;
        private final Runnable longPressRunnable;
        private final Runnable tapRunnable;

        EdgeGestureView(GestureAccessibilityService service, int side) {
            super(service);
            this.service = service;
            this.side = side;
            this.longPressRunnable = () -> {
                String key = longPressKey();
                if (this.touchActive && !this.longTriggered && this.service.isEnabled(key)) {
                    this.longTriggered = true;
                    this.service.triggerGesture(key);
                }
            };
            this.tapRunnable = () -> {
                if (this.tapCount == 2) this.service.triggerGesture(doubleTapKey());
                resetTapSequence();
            };
            setBackgroundColor(android.graphics.Color.TRANSPARENT);
            setClickable(true);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    touchActive = true;
                    downX = event.getRawX();
                    downY = event.getRawY();
                    downAt = SystemClock.uptimeMillis();
                    longTriggered = false;
                    postDelayed(longPressRunnable, LONG_PRESS_MS);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!touchActive) return true;
                    if (Math.abs(event.getRawX() - downX) > dp(24) || Math.abs(event.getRawY() - downY) > dp(24)) {
                        removeCallbacks(longPressRunnable);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!touchActive) return true;
                    touchActive = false;
                    removeCallbacks(longPressRunnable);
                    if (longTriggered) return true;
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    long elapsed = SystemClock.uptimeMillis() - downAt;
                    float min = dp((int) SWIPE_MIN_DP);
                    if (Math.abs(dx) < dp(28) && Math.abs(dy) < dp(28) && elapsed < 450L) {
                        performClick();
                        handleTap();
                        return true;
                    }
                    resetTapSequence();
                    float inward = side == SIDE_LEFT ? dx : -dx;
                    if (isSideEdge() && inward >= min && Math.abs(dy) >= min) {
                        String diagonalKey;
                        if (side == SIDE_LEFT) {
                            diagonalKey = dy < 0 ? AppConfig.LEFT_IN_UP : AppConfig.LEFT_IN_DOWN;
                        } else {
                            diagonalKey = dy < 0 ? AppConfig.RIGHT_IN_UP : AppConfig.RIGHT_IN_DOWN;
                        }
                        if (service.isEnabled(diagonalKey)) {
                            service.triggerGesture(diagonalKey);
                            return true;
                        }
                    }
                    if (side == SIDE_BOTTOM && -dy >= min && Math.abs(dx) >= min) {
                        String diagonalKey = dx < 0 ? AppConfig.BOTTOM_UP_LEFT : AppConfig.BOTTOM_UP_RIGHT;
                        if (service.isEnabled(diagonalKey)) {
                            service.triggerGesture(diagonalKey);
                            return true;
                        }
                    }
                    if (side == SIDE_BOTTOM && Math.abs(dx) >= min && Math.abs(dx) > Math.abs(dy)) {
                        service.triggerGesture(dx < 0 ? AppConfig.BOTTOM_LEFT : AppConfig.BOTTOM_RIGHT);
                    } else if (side == SIDE_BOTTOM && -dy >= min && Math.abs(dy) > Math.abs(dx)) {
                        service.triggerGesture(AppConfig.BOTTOM_UP);
                    } else if (isTopEdge() && dy >= min && Math.abs(dy) > Math.abs(dx)) {
                        service.triggerGesture(side == SIDE_TOP_LEFT
                                ? AppConfig.TOP_LEFT_DOWN : AppConfig.TOP_RIGHT_DOWN);
                    } else if (side == SIDE_LEFT && dx >= min && Math.abs(dx) > Math.abs(dy)) {
                        service.triggerGesture(AppConfig.LEFT_IN);
                    } else if (side == SIDE_RIGHT && -dx >= min && Math.abs(dx) > Math.abs(dy)) {
                        service.triggerGesture(AppConfig.RIGHT_IN);
                    } else if (isSideEdge() && Math.abs(dy) >= min && Math.abs(dy) > Math.abs(dx)) {
                        String key;
                        if (side == SIDE_LEFT) key = dy < 0 ? AppConfig.LEFT_UP : AppConfig.LEFT_DOWN;
                        else key = dy < 0 ? AppConfig.RIGHT_UP : AppConfig.RIGHT_DOWN;
                        service.triggerGesture(key);
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    cancelPendingGesture();
                    return true;
                default:
                    return true;
            }
        }

        @Override
        public boolean performClick() {
            // Publish the standard click event without inventing a single-tap system action.
            super.performClick();
            return true;
        }

        private void handleTap() {
            long now = SystemClock.uptimeMillis();
            removeCallbacks(tapRunnable);
            tapCount = now - lastTapAt <= DOUBLE_TAP_MS ? tapCount + 1 : 1;
            lastTapAt = now;
            String tripleKey = tripleTapKey();
            if (tapCount >= 3 && service.isEnabled(tripleKey)) {
                service.triggerGesture(tripleKey);
                resetTapSequence();
            } else if (tapCount >= 2) {
                if (service.isEnabled(tripleKey)) {
                    postDelayed(tapRunnable, DOUBLE_TAP_MS);
                } else {
                    service.triggerGesture(doubleTapKey());
                    resetTapSequence();
                }
            }
        }

        private String doubleTapKey() {
            if (side == SIDE_LEFT) return AppConfig.LEFT_DOUBLE;
            if (side == SIDE_RIGHT) return AppConfig.RIGHT_DOUBLE;
            if (side == SIDE_BOTTOM) return AppConfig.BOTTOM_DOUBLE;
            if (side == SIDE_TOP_LEFT) return AppConfig.TOP_LEFT_DOUBLE;
            return AppConfig.TOP_RIGHT_DOUBLE;
        }

        private String tripleTapKey() {
            if (side == SIDE_LEFT) return AppConfig.LEFT_TRIPLE;
            if (side == SIDE_RIGHT) return AppConfig.RIGHT_TRIPLE;
            if (side == SIDE_BOTTOM) return AppConfig.BOTTOM_TRIPLE;
            return AppConfig.NONE;
        }

        private String longPressKey() {
            if (side == SIDE_LEFT) return AppConfig.LEFT_LONG;
            if (side == SIDE_RIGHT) return AppConfig.RIGHT_LONG;
            if (side == SIDE_BOTTOM) return AppConfig.BOTTOM_LONG;
            if (side == SIDE_TOP_LEFT) return AppConfig.TOP_LEFT_LONG;
            return AppConfig.TOP_RIGHT_LONG;
        }

        private boolean isSideEdge() {
            return side == SIDE_LEFT || side == SIDE_RIGHT;
        }

        private boolean isTopEdge() {
            return side == SIDE_TOP_LEFT || side == SIDE_TOP_RIGHT;
        }

        private void resetTapSequence() {
            removeCallbacks(tapRunnable);
            tapCount = 0;
            lastTapAt = 0L;
        }

        private int dp(int value) {
            return Math.round(value * getResources().getDisplayMetrics().density);
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            super.onSizeChanged(width, height, oldWidth, oldHeight);
            if (isSideEdge()) {
                // Android reserves the outer edge for Back. Keep the exclusion within
                // its 200dp budget and preserve native Back above/below our trigger zone.
                int zoneHeight = Math.min(height, dp(200));
                int top = (height - zoneHeight) / 2;
                setSystemGestureExclusionRects(Collections.singletonList(
                        new Rect(0, top, width, top + zoneHeight)));
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            cancelPendingGesture();
            super.onDetachedFromWindow();
        }

        void cancelPendingGesture() {
            touchActive = false;
            removeCallbacks(longPressRunnable);
            resetTapSequence();
            longTriggered = false;
        }
    }
}
