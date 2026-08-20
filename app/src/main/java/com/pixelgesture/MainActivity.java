package com.pixelgesture;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Localized, lazy-loading settings screen for the gesture service. */
public class MainActivity extends Activity {
    private static final int BG = Color.rgb(16, 16, 22);
    private static final int SURFACE = Color.rgb(30, 27, 35);
    private static final int SURFACE_VARIANT = Color.rgb(43, 41, 50);
    private static final int PRIMARY = Color.rgb(208, 188, 255);
    private static final int ON_PRIMARY = Color.rgb(56, 30, 114);
    private static final int TEXT = Color.rgb(230, 224, 233);
    private static final int SECONDARY = Color.rgb(202, 196, 208);

    private static final class AppListCache {
        final List<String> packageNames;
        final CharSequence[] labels;

        AppListCache(List<String> packageNames, CharSequence[] labels) {
            this.packageNames = packageNames;
            this.labels = labels;
        }
    }

    private static final class GestureSpec {
        final String key;
        final int title;
        final int description;

        GestureSpec(String key, int title, int description) {
            this.key = key;
            this.title = title;
            this.description = description;
        }
    }

    private static GestureSpec g(String key, int title, int description) {
        return new GestureSpec(key, title, description);
    }

    private static final GestureSpec[] CORE_GESTURES = {
            g(AppConfig.LEFT_IN, R.string.g_left_in, R.string.d_left_in),
            g(AppConfig.LEFT_UP, R.string.g_left_up, R.string.d_left_up),
            g(AppConfig.LEFT_DOWN, R.string.g_left_down, R.string.d_left_down),
            g(AppConfig.LEFT_DOUBLE, R.string.g_left_double, R.string.d_left_double),
            g(AppConfig.RIGHT_IN, R.string.g_right_in, R.string.d_right_in),
            g(AppConfig.RIGHT_UP, R.string.g_right_up, R.string.d_right_up),
            g(AppConfig.RIGHT_DOWN, R.string.g_right_down, R.string.d_right_down),
            g(AppConfig.RIGHT_DOUBLE, R.string.g_right_double, R.string.d_right_double),
            g(AppConfig.BOTTOM_UP, R.string.g_bottom_up, R.string.d_bottom_up),
            g(AppConfig.BOTTOM_LONG, R.string.g_bottom_long, R.string.d_bottom_long),
            g(AppConfig.BOTTOM_DOUBLE, R.string.g_bottom_double, R.string.d_bottom_double)
    };

    private static final GestureSpec[] ADVANCED_GESTURES = {
            g(AppConfig.LEFT_LONG, R.string.g_left_long, R.string.d_left_long),
            g(AppConfig.RIGHT_LONG, R.string.g_right_long, R.string.d_right_long),
            g(AppConfig.LEFT_IN_UP, R.string.g_left_in_up, R.string.d_left_in_up),
            g(AppConfig.LEFT_IN_DOWN, R.string.g_left_in_down, R.string.d_left_in_down),
            g(AppConfig.RIGHT_IN_UP, R.string.g_right_in_up, R.string.d_right_in_up),
            g(AppConfig.RIGHT_IN_DOWN, R.string.g_right_in_down, R.string.d_right_in_down),
            g(AppConfig.BOTTOM_LEFT, R.string.g_bottom_left, R.string.d_bottom_left),
            g(AppConfig.BOTTOM_RIGHT, R.string.g_bottom_right, R.string.d_bottom_right),
            g(AppConfig.BOTTOM_UP_LEFT, R.string.g_bottom_up_left, R.string.d_bottom_up_left),
            g(AppConfig.BOTTOM_UP_RIGHT, R.string.g_bottom_up_right, R.string.d_bottom_up_right),
            g(AppConfig.LEFT_TRIPLE, R.string.g_left_triple, R.string.d_left_triple),
            g(AppConfig.RIGHT_TRIPLE, R.string.g_right_triple, R.string.d_right_triple),
            g(AppConfig.BOTTOM_TRIPLE, R.string.g_bottom_triple, R.string.d_bottom_triple)
    };

    private static final GestureSpec[] TOP_GESTURES = {
            g(AppConfig.TOP_LEFT_DOWN, R.string.g_top_left_down, R.string.d_top_left_down),
            g(AppConfig.TOP_LEFT_DOUBLE, R.string.g_top_left_double, R.string.d_top_left_double),
            g(AppConfig.TOP_LEFT_LONG, R.string.g_top_left_long, R.string.d_top_left_long),
            g(AppConfig.TOP_RIGHT_DOWN, R.string.g_top_right_down, R.string.d_top_right_down),
            g(AppConfig.TOP_RIGHT_DOUBLE, R.string.g_top_right_double, R.string.d_top_right_double),
            g(AppConfig.TOP_RIGHT_LONG, R.string.g_top_right_long, R.string.d_top_right_long)
    };

    private static final GestureSpec[] HARDWARE_GESTURES = {
            g(AppConfig.SHAKE, R.string.g_shake, R.string.d_shake),
            g(AppConfig.VOLUME_CHORD, R.string.g_volume_chord, R.string.d_volume_chord),
            g(AppConfig.VOLUME_UP_DOUBLE, R.string.g_volume_up_double, R.string.d_volume_up_double),
            g(AppConfig.VOLUME_DOWN_DOUBLE, R.string.g_volume_down_double, R.string.d_volume_down_double),
            g(AppConfig.VOLUME_UP_LONG, R.string.g_volume_up_long, R.string.d_volume_up_long),
            g(AppConfig.VOLUME_DOWN_LONG, R.string.g_volume_down_long, R.string.d_volume_down_long)
    };

    private android.content.SharedPreferences prefs;
    private TextView serviceStatus;
    private Button serviceButton;
    private String[] actionLabels;
    private List<String> cachedPackageNames;
    private CharSequence[] cachedAppLabels;
    private final ExecutorService appListExecutor = Executors.newSingleThreadExecutor();
    private boolean appCacheLoading;
    private String pendingGestureKey;
    private Button pendingAppButton;
    private final Map<Button, String> waitingAppButtons = new LinkedHashMap<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        prefs = getSharedPreferences(AppConfig.PREFS, MODE_PRIVATE);
        AppConfig.ensureDefaults(prefs);
        LanguageManager.syncPlatformLocale(this);
        actionLabels = getResources().getStringArray(R.array.action_labels);
        setContentView(buildScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceState();
    }

    @Override
    protected void onDestroy() {
        pendingAppButton = null;
        waitingAppButtons.clear();
        appListExecutor.shutdownNow();
        super.onDestroy();
    }

    private View buildScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(18), dp(20), dp(36));
        scrollView.addView(page, new ViewGroup.LayoutParams(-1, -2));

        TextView title = text(getString(R.string.app_name), 30, TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        page.addView(title, marginParams(-1, -2, 0, 0, 0, 4));
        page.addView(text(getString(R.string.subtitle), 15, SECONDARY), marginParams(-1, -2, 0, 0, 0, 18));

        addStatusCard(page);
        addLanguageCard(page);
        addMasterCard(page);
        addGestureSection(page, R.string.section_edge_title, R.string.section_edge_description, true, CORE_GESTURES);
        addGestureSection(page, R.string.section_advanced_title, R.string.section_advanced_description, false, ADVANCED_GESTURES);
        addGestureSection(page, R.string.section_top_title, R.string.section_top_description, false, TOP_GESTURES);
        addGestureSection(page, R.string.section_hardware_title, R.string.section_hardware_description, false, HARDWARE_GESTURES);
        addTuningCard(page);

        TextView footer = text(getString(R.string.footer), 12, SECONDARY);
        footer.setGravity(Gravity.CENTER);
        page.addView(footer, marginParams(-1, -2, 0, 6, 0, 0));
        return scrollView;
    }

    private void addStatusCard(LinearLayout page) {
        LinearLayout card = card();
        TextView title = text(getString(R.string.service_status_title), 18, TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title, marginParams(-1, -2, 0, 0, 0, 8));
        serviceStatus = text(getString(R.string.checking), 14, SECONDARY);
        card.addView(serviceStatus, marginParams(-1, -2, 0, 0, 0, 14));
        serviceButton = primaryButton(getString(R.string.open_accessibility_settings));
        serviceButton.setOnClickListener(v -> openAccessibilitySettings());
        card.addView(serviceButton, new LinearLayout.LayoutParams(-1, dp(48)));
        Button details = secondaryButton(getString(R.string.open_app_details));
        details.setOnClickListener(v -> openAppDetails());
        card.addView(details, marginParams(-1, dp(48), 0, 8, 0, 0));
        card.addView(text(getString(R.string.restricted_settings_help), 12, SECONDARY), marginParams(-1, -2, 0, 12, 0, 0));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addLanguageCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.language_title, R.string.language_description);
        Spinner spinner = new Spinner(this);
        String[] languages = {getString(R.string.language_english), getString(R.string.language_chinese)};
        ArrayAdapter<String> adapter = styledAdapter(languages);
        spinner.setAdapter(adapter);
        String current = prefs.getString(AppConfig.LANGUAGE, AppConfig.LANGUAGE_ENGLISH);
        spinner.setSelection(AppConfig.LANGUAGE_CHINESE.equals(current) ? 1 : 0);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = position == 1 ? AppConfig.LANGUAGE_CHINESE : AppConfig.LANGUAGE_ENGLISH;
                String old = prefs.getString(AppConfig.LANGUAGE, AppConfig.LANGUAGE_ENGLISH);
                if (!selected.equals(old)) {
                    LanguageManager.setLanguage(MainActivity.this, selected);
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) recreate();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        card.addView(spinner, new LinearLayout.LayoutParams(-1, dp(48)));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addMasterCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.master_title, R.string.master_description);
        Switch master = new Switch(this);
        master.setText(getString(R.string.master_switch));
        master.setTextColor(TEXT);
        master.setTextSize(14);
        master.setChecked(prefs.getBoolean(AppConfig.MASTER_ENABLED, true));
        master.setOnCheckedChangeListener((buttonView, checked) ->
                prefs.edit().putBoolean(AppConfig.MASTER_ENABLED, checked).apply());
        card.addView(master, new LinearLayout.LayoutParams(-1, dp(48)));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addGestureSection(LinearLayout page, int titleRes, int descriptionRes,
                                   boolean initiallyExpanded, GestureSpec[] specs) {
        LinearLayout card = card();
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(getString(titleRes), 18, TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        heading.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button toggle = secondaryButton(getString(initiallyExpanded ? R.string.hide_section : R.string.show_section));
        heading.addView(toggle, new LinearLayout.LayoutParams(dp(88), dp(42)));
        card.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text(getString(descriptionRes), 12, SECONDARY), marginParams(-1, -2, 0, 6, 0, 12));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        card.addView(content, new LinearLayout.LayoutParams(-1, -2));
        boolean[] built = {false};
        Runnable buildRows = () -> {
            if (built[0]) return;
            built[0] = true;
            for (int i = 0; i < specs.length; i++) addBinding(content, specs[i], i == 0);
        };
        if (initiallyExpanded) {
            buildRows.run();
            content.setVisibility(View.VISIBLE);
        } else {
            content.setVisibility(View.GONE);
        }
        toggle.setOnClickListener(v -> {
            boolean show = content.getVisibility() != View.VISIBLE;
            if (show) buildRows.run();
            content.setVisibility(show ? View.VISIBLE : View.GONE);
            toggle.setText(show ? R.string.hide_section : R.string.show_section);
        });
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addTuningCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.section_tuning_title, R.string.section_tuning_description);
        TextView widthLabel = text(getString(R.string.edge_width_format, prefs.getInt(AppConfig.EDGE_WIDTH, 18)), 14, TEXT);
        card.addView(widthLabel, marginParams(-1, -2, 0, 0, 0, 4));
        SeekBar widthBar = new SeekBar(this);
        widthBar.setMax(28);
        widthBar.setProgress(Math.max(0, Math.min(28, prefs.getInt(AppConfig.EDGE_WIDTH, 18) - 12)));
        widthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int value = 12 + progress;
                widthLabel.setText(getString(R.string.edge_width_format, value));
                if (fromUser) prefs.edit().putInt(AppConfig.EDGE_WIDTH, value).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        card.addView(widthBar, marginParams(-1, dp(48), 0, 0, 0, 4));
        Switch haptic = new Switch(this);
        haptic.setText(getString(R.string.haptic_feedback));
        haptic.setTextColor(TEXT);
        haptic.setTextSize(14);
        haptic.setChecked(prefs.getBoolean(AppConfig.HAPTIC, true));
        haptic.setOnCheckedChangeListener((buttonView, checked) -> prefs.edit().putBoolean(AppConfig.HAPTIC, checked).apply());
        card.addView(haptic, new LinearLayout.LayoutParams(-1, dp(48)));
        TextView resetTitle = text(getString(R.string.reset_title), 15, TEXT);
        resetTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(resetTitle, marginParams(-1, -2, 0, 12, 0, 4));
        card.addView(text(getString(R.string.reset_description), 12, SECONDARY), marginParams(-1, -2, 0, 0, 0, 8));
        Button reset = secondaryButton(getString(R.string.reset_button));
        reset.setOnClickListener(v -> confirmReset());
        card.addView(reset, new LinearLayout.LayoutParams(-1, dp(48)));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addBinding(LinearLayout parent, GestureSpec spec, boolean first) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(roundRect(SURFACE_VARIANT, 18, 0, Color.TRANSPARENT));
        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = text(getString(spec.title), 15, TEXT);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        heading.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        Switch enabled = new Switch(this);
        enabled.setChecked(prefs.getBoolean(AppConfig.enabledKey(spec.key), false));
        enabled.setOnCheckedChangeListener((buttonView, checked) ->
                prefs.edit().putBoolean(AppConfig.enabledKey(spec.key), checked).apply());
        heading.addView(enabled, new LinearLayout.LayoutParams(dp(56), dp(48)));
        row.addView(heading, new LinearLayout.LayoutParams(-1, dp(48)));
        row.addView(text(getString(spec.description), 12, SECONDARY), marginParams(-1, -2, 0, 6, 0, 6));

        Spinner spinner = new Spinner(this);
        spinner.setAdapter(styledAdapter(actionLabels));
        String current = prefs.getString(spec.key, AppConfig.NONE);
        int selected = 0;
        for (int i = 0; i < AppConfig.ACTION_KEYS.length; i++) {
            if (AppConfig.ACTION_KEYS[i].equals(current)) { selected = i; break; }
        }
        spinner.setSelection(selected);
        Button appButton = secondaryButton(appButtonLabel(spec.key));
        appButton.setOnClickListener(v -> showAppPicker(spec.key, appButton));
        appButton.setVisibility(AppConfig.OPEN_APP.equals(current) ? View.VISIBLE : View.GONE);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parentView, View view, int position, long id) {
                String action = AppConfig.ACTION_KEYS[position];
                String previous = prefs.getString(spec.key, AppConfig.NONE);
                if (!action.equals(previous)) prefs.edit().putString(spec.key, action).apply();
                appButton.setVisibility(AppConfig.OPEN_APP.equals(action) ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parentView) { }
        });
        row.addView(spinner, new LinearLayout.LayoutParams(-1, dp(48)));
        row.addView(appButton, marginParams(-1, dp(48), 0, 6, 0, 0));
        parent.addView(row, marginParams(-1, -2, 0, first ? 4 : 6, 0, 0));
    }

    private ArrayAdapter<String> styledAdapter(String[] labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(TEXT);
                view.setTextSize(14);
                view.setPadding(dp(12), 0, dp(8), 0);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private String appButtonLabel(String gestureKey) {
        String packageName = prefs.getString(AppConfig.appPackageKey(gestureKey), "");
        if (packageName == null || packageName.isEmpty()) return getString(R.string.choose_app);
        try {
            CharSequence label = getPackageManager().getApplicationLabel(getApplicationInfoCompat(packageName));
            return getString(R.string.selected_app_format, label);
        } catch (PackageManager.NameNotFoundException ignored) {
            return getString(R.string.selected_app_unavailable);
        }
    }

    private void showAppPicker(String gestureKey, Button appButton) {
        if (cachedPackageNames == null) {
            loadAppCacheThenShow(gestureKey, appButton);
            return;
        }
        showAppPickerDialog(gestureKey, appButton);
    }

    private void showAppPickerDialog(String gestureKey, Button appButton) {
        if (cachedPackageNames == null || cachedPackageNames.isEmpty()) {
            Toast.makeText(this, R.string.no_launchable_apps, Toast.LENGTH_SHORT).show();
            return;
        }
        String currentPackage = prefs.getString(AppConfig.appPackageKey(gestureKey), "");
        int checkedItem = cachedPackageNames.indexOf(currentPackage);
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_launchable_app)
                .setSingleChoiceItems(cachedAppLabels, checkedItem, (dialog, which) -> {
                    prefs.edit().putString(AppConfig.appPackageKey(gestureKey), cachedPackageNames.get(which)).apply();
                    appButton.setText(appButtonLabel(gestureKey));
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadAppCacheThenShow(String gestureKey, Button appButton) {
        pendingGestureKey = gestureKey;
        pendingAppButton = appButton;
        waitingAppButtons.put(appButton, gestureKey);
        appButton.setEnabled(false);
        appButton.setText(R.string.loading_apps);
        if (appCacheLoading) return;
        appCacheLoading = true;
        appListExecutor.execute(() -> {
            AppListCache result;
            try {
                result = buildAppCache();
            } catch (RuntimeException ignored) {
                result = new AppListCache(new ArrayList<>(), new CharSequence[0]);
            }
            AppListCache loaded = result;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;
                cachedPackageNames = loaded.packageNames;
                cachedAppLabels = loaded.labels;
                appCacheLoading = false;
                Button targetButton = pendingAppButton;
                String targetGesture = pendingGestureKey;
                for (Map.Entry<Button, String> waiting : waitingAppButtons.entrySet()) {
                    waiting.getKey().setEnabled(true);
                    waiting.getKey().setText(appButtonLabel(waiting.getValue()));
                }
                waitingAppButtons.clear();
                pendingAppButton = null;
                pendingGestureKey = null;
                if (targetButton != null && targetGesture != null) {
                    showAppPickerDialog(targetGesture, targetButton);
                }
            });
        });
    }

    private AppListCache buildAppCache() {
        PackageManager packageManager = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolved = new ArrayList<>(queryLauncherApps(packageManager, query));
        resolved.sort(new ResolveInfo.DisplayNameComparator(packageManager));
        Map<String, ResolveInfo> unique = new LinkedHashMap<>();
        for (ResolveInfo info : resolved) {
            if (info.activityInfo != null) unique.putIfAbsent(info.activityInfo.packageName, info);
        }
        List<String> packageNames = new ArrayList<>(unique.keySet());
        List<String> rawLabels = new ArrayList<>();
        Map<String, Integer> counts = new HashMap<>();
        for (ResolveInfo info : unique.values()) {
            String label = String.valueOf(info.loadLabel(packageManager));
            rawLabels.add(label);
            counts.put(label, counts.containsKey(label) ? counts.get(label) + 1 : 1);
        }
        CharSequence[] labels = new CharSequence[rawLabels.size()];
        for (int i = 0; i < rawLabels.size(); i++) {
            String label = rawLabels.get(i);
            labels[i] = counts.get(label) > 1 ? label + " · " + packageNames.get(i) : label;
        }
        return new AppListCache(packageNames, labels);
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reset_confirm_title)
                .setMessage(R.string.reset_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reset_confirm, (dialog, which) -> {
                    AppConfig.resetToSafeDefaults(prefs);
                    Toast.makeText(this, R.string.reset_done, Toast.LENGTH_SHORT).show();
                    recreate();
                })
                .show();
    }

    private void addSectionTitle(LinearLayout parent, int titleRes, int descriptionRes) {
        TextView title = text(getString(titleRes), 18, TEXT);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        parent.addView(title, marginParams(-1, -2, 0, 0, 0, 4));
        parent.addView(text(getString(descriptionRes), 12, SECONDARY), marginParams(-1, -2, 0, 0, 0, 12));
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(roundRect(SURFACE, 24, 1, Color.rgb(55, 52, 61)));
        return card;
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(ON_PRIMARY);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackground(roundRect(PRIMARY, 24, 0, Color.TRANSPARENT));
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(PRIMARY);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackground(roundRect(SURFACE_VARIANT, 24, 1, Color.rgb(73, 69, 79)));
        return button;
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private GradientDrawable roundRect(int color, int radiusDp, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeWidth > 0) drawable.setStroke(dp(strokeWidth), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams marginParams(int width, int height, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void openAccessibilitySettings() {
        try {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (Exception ignored) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void openAppDetails() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private boolean isServiceEnabled() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        List<AccessibilityServiceInfo> services = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        String expected = getPackageName() + "/" + GestureAccessibilityService.class.getName();
        for (AccessibilityServiceInfo info : services) {
            if (info.getResolveInfo() != null && info.getResolveInfo().serviceInfo != null) {
                String actual = info.getResolveInfo().serviceInfo.packageName + "/" + info.getResolveInfo().serviceInfo.name;
                if (expected.equals(actual)) return true;
            }
        }
        return false;
    }

    private void updateServiceState() {
        if (serviceStatus == null) return;
        boolean enabled = isServiceEnabled();
        serviceStatus.setText(enabled ? R.string.service_enabled : R.string.service_disabled);
        serviceStatus.setTextColor(enabled ? Color.rgb(182, 242, 194) : SECONDARY);
        serviceButton.setText(enabled ? R.string.manage_accessibility_service : R.string.open_accessibility_settings);
    }

    @SuppressWarnings("deprecation")
    private ApplicationInfo getApplicationInfoCompat(String packageName) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0));
        }
        return getPackageManager().getApplicationInfo(packageName, 0);
    }

    @SuppressWarnings("deprecation")
    private List<ResolveInfo> queryLauncherApps(PackageManager packageManager, Intent query) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return packageManager.queryIntentActivities(query, PackageManager.ResolveInfoFlags.of(0));
        }
        return packageManager.queryIntentActivities(query, 0);
    }
}
