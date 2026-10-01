package com.pixelgesture;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.Drawable;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Localized, lazy-loading settings screen for the gesture service. */
public class MainActivity extends Activity {
    private BillingController billing;
    private ProfileStore profiles;
    private TextView billingStatus;
    private Button buyProfiles, saveProfile, restorePurchases, refreshPurchases;
    private LinearLayout profileRows;
    private static final int EXPORT_SETTINGS = 100;
    private static final int IMPORT_SETTINGS = 101;
    private int BG, SURFACE, SURFACE_VARIANT, PRIMARY, ON_PRIMARY, TEXT, SECONDARY, OUTLINE;
    private int currentTab;
    private boolean practiceShown, practiceFromTouch;
    private boolean settingsExpanded;
    private boolean darkTheme;
    private GestureSpec editingGesture;

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
    private TextView serviceStatus, serviceDetailStatus;
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
        super.attachBaseContext(ThemeManager.wrap(LanguageManager.wrap(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        darkTheme=(getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;
        BG = getColor(R.color.pg_background); SURFACE = getColor(R.color.pg_surface);
        SURFACE_VARIANT = getColor(R.color.pg_surface_variant); PRIMARY = getColor(R.color.pg_primary);
        ON_PRIMARY = getColor(R.color.pg_on_primary); TEXT = getColor(R.color.pg_on_surface);
        SECONDARY = getColor(R.color.pg_on_surface_variant); OUTLINE = getColor(R.color.pg_outline);
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt("uiTab", 0);
            practiceShown=savedInstanceState.getBoolean("practiceShown");
            practiceFromTouch=savedInstanceState.getBoolean("practiceFromTouch");
            settingsExpanded=savedInstanceState.getBoolean("settingsExpanded");
            editingGesture = findGesture(savedInstanceState.getString("uiGesture"));
        }
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(SURFACE);
        prefs = getSharedPreferences(AppConfig.PREFS, MODE_PRIVATE);
        AppConfig.ensureDefaults(prefs);
        billing = new BillingController(GatewayFactory.create(this), this::renderProfiles);
        profiles = new ProfileStore(getSharedPreferences(ProfileStore.PREFS, MODE_PRIVATE), billing);
        LanguageManager.syncPlatformLocale(this);
        actionLabels = getResources().getStringArray(R.array.action_labels);
        setContentView(buildScreen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceState();
        if (!billing.busy()) billing.refresh();
    }

    @Override
    protected void onDestroy() {
        if (billing != null) billing.close();
        pendingAppButton = null;
        waitingAppButtons.clear();
        appListExecutor.shutdownNow();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt("uiTab", currentTab);
        state.putBoolean("practiceShown",practiceShown);state.putBoolean("practiceFromTouch",practiceFromTouch);state.putBoolean("settingsExpanded",settingsExpanded);
        if (editingGesture != null) state.putString("uiGesture", editingGesture.key);
        super.onSaveInstanceState(state);
    }

    @Override
    public void onBackPressed() {
        if (editingGesture != null) { editingGesture = null; showTab(1); }
        else if (currentTab != 0) showTab(0);
        else super.onBackPressed();
    }

    private GestureSpec findGesture(String key) {
        for (GestureSpec[] group : new GestureSpec[][] {CORE_GESTURES, ADVANCED_GESTURES, TOP_GESTURES, HARDWARE_GESTURES})
            for (GestureSpec spec : group) if (spec.key.equals(key)) return spec;
        return null;
    }

    private void showTab(int tab) {
        currentTab = tab;
        pendingAppButton = null;
        pendingGestureKey = null;
        waitingAppButtons.clear();
        setContentView(buildScreen());
        updateServiceState();
    }

    private View buildScreen() {
        serviceStatus = null; serviceDetailStatus=null; serviceButton = null; billingStatus = null; profileRows = null;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(24), dp(24), dp(28));
        scroll.addView(page, new ViewGroup.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        if (currentTab == 0) {
            addPracticeHome(page);
        } else if (currentTab == 1 && editingGesture != null) {
            Button back = secondaryButton(getString(R.string.ui_back_gestures));
            back.setOnClickListener(v -> { editingGesture = null; showTab(1); });
            page.addView(back, marginParams(-1,-2,0,0,0,20));
            addPageHeader(page, R.string.ui_editor_eyebrow, R.string.ui_editor_title, R.string.ui_editor_subtitle);
            LinearLayout editor = card(); addBinding(editor, editingGesture, true);
            page.addView(editor, marginParams(-1,-2,0,0,0,16));
            LinearLayout note = card(); addSectionTitle(note, R.string.ui_input_note, R.string.ui_input_note_help);
            page.addView(note, marginParams(-1,-2,0,0,0,16));
        } else if (currentTab == 1) {
            addPageHeader(page, R.string.ui_gestures_eyebrow, R.string.ui_gestures_title, R.string.ui_gestures_subtitle);
            addGestureSection(page, R.string.section_edge_title, R.string.section_edge_description, true, CORE_GESTURES);
            addGestureSection(page, R.string.section_advanced_title, R.string.section_advanced_description, false, ADVANCED_GESTURES);
            addGestureSection(page, R.string.section_top_title, R.string.section_top_description, false, TOP_GESTURES);
            addGestureSection(page, R.string.section_hardware_title, R.string.section_hardware_description, false, HARDWARE_GESTURES);
            addTuningCard(page);
        } else {
            addPageHeader(page, R.string.ui_profiles_eyebrow, R.string.ui_profiles_title, R.string.ui_profiles_subtitle);
            addProfilesCard(page); addBackupCard(page);
        }
        page.addView(text(getString(R.string.ui_footer),12,SECONDARY), marginParams(-1,-2,0,8,0,0));
        LinearLayout nav = new LinearLayout(this); nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(4),dp(8),dp(4),dp(8)); nav.setBackgroundColor(SURFACE);
        int[] labels = {R.string.ui_tab_home,R.string.ui_tab_gestures,R.string.ui_tab_profiles};
        int[] icons = {R.drawable.ic_ui_home,R.drawable.ic_ui_gesture,R.drawable.ic_ui_profiles};
        for (int i=0;i<3;i++) {
            final int tab=i; Button item = secondaryButton(getString(labels[i]));
            item.setTextSize(12); item.setPadding(dp(2),dp(8),dp(2),dp(8));
            item.setSingleLine(true); item.setEllipsize(android.text.TextUtils.TruncateAt.END);
            item.setTextColor(i==currentTab ? TEXT : SECONDARY); item.setSelected(i==currentTab);
            item.setBackground(buttonBackground(i==currentTab ? SURFACE_VARIANT : SURFACE,SURFACE));
            Drawable icon=getDrawable(icons[i]).mutate(); icon.setTint(i==currentTab ? PRIMARY : SECONDARY);
            icon.setBounds(0,0,dp(22),dp(22)); item.setCompoundDrawables(null,icon,null,null); item.setCompoundDrawablePadding(dp(4));
            item.setOnClickListener(v -> { editingGesture=null; showTab(tab); });
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1); lp.setMargins(dp(2),0,dp(2),0);nav.addView(item,lp);
        }
        root.addView(nav,new LinearLayout.LayoutParams(-1,-2));return root;
    }

    private void addPracticeHome(LinearLayout page) {
        TextView brand=text("PixelGesture",25,TEXT);brand.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        page.addView(brand,marginParams(-1,-2,0,0,0,6));
        page.addView(text(getString(R.string.ui_practice_kicker),13,SECONDARY),marginParams(-1,-2,0,0,0,18));
        LinearLayout status=new LinearLayout(this);status.setGravity(Gravity.CENTER_VERTICAL);
        boolean stackedStatus=getResources().getConfiguration().fontScale>1.3f
                || getResources().getConfiguration().screenWidthDp<350;
        if(stackedStatus)status.setOrientation(LinearLayout.VERTICAL);
        serviceStatus=text("",13,SECONDARY);serviceStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        status.addView(serviceStatus,stackedStatus?new LinearLayout.LayoutParams(-1,-2)
                :new LinearLayout.LayoutParams(0,-2,1));
        Switch master=new Switch(this);master.setText(R.string.ui_home_master);master.setTextSize(13);master.setTextColor(TEXT);master.setMinimumHeight(dp(48));
        master.setChecked(prefs.getBoolean(AppConfig.MASTER_ENABLED,true));master.setOnCheckedChangeListener((button,checked)->{prefs.edit().putBoolean(AppConfig.MASTER_ENABLED,checked).apply();updateServiceState();});
        status.addView(master,new LinearLayout.LayoutParams(-2,-2));page.addView(status,marginParams(-1,-2,0,0,0,14));
        TextView title=text(getString(R.string.ui_practice_title),28,TEXT);title.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));title.setAccessibilityHeading(true);
        page.addView(title,marginParams(-1,-2,0,0,0,6));
        page.addView(text(getString(R.string.ui_practice_scope),13,SECONDARY),marginParams(-1,-2,0,0,0,10));
        TextView result=text(getString(practiceShown?(practiceFromTouch?R.string.ui_practice_swiped:R.string.ui_practice_shown):R.string.ui_practice_hint),14,TEXT);
        result.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        GesturePracticeView practice=new GesturePracticeView(this,practiceShown,(shown,fromTouch)->{
            practiceShown=shown;practiceFromTouch=fromTouch;
            result.setText(shown?(fromTouch?R.string.ui_practice_swiped:R.string.ui_practice_shown):R.string.ui_practice_hint);
        });
        page.addView(practice,new LinearLayout.LayoutParams(-1,dp(300)));
        page.addView(result,marginParams(-1,-2,0,10,0,12));
        LinearLayout actions=new LinearLayout(this);
        Button preview=secondaryButton(getString(R.string.ui_practice_play));preview.setOnClickListener(v->practice.showPreview(false));
        Button reset=secondaryButton(getString(R.string.ui_practice_reset));reset.setOnClickListener(v->practice.reset());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,dp(4),0);actions.addView(preview,lp);
        lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(4),0,0,0);actions.addView(reset,lp);
        page.addView(actions,marginParams(-1,-2,0,0,0,12));
        Button edit=primaryButton(getString(R.string.ui_edit_gestures));edit.setOnClickListener(v->{editingGesture=null;showTab(1);});
        page.addView(edit,marginParams(-1,-2,0,0,0,12));
        LinearLayout settings=new LinearLayout(this);settings.setOrientation(LinearLayout.VERTICAL);settings.setVisibility(settingsExpanded?View.VISIBLE:View.GONE);
        // Permission details remain discoverable; entering settings does not grant access.
        TextView compactStatus=serviceStatus;addStatusCard(settings);serviceDetailStatus=serviceStatus;addLanguageCard(settings);addThemeCard(settings);serviceStatus=compactStatus;
        Button controls=secondaryButton(getString(R.string.ui_home_settings));controls.setOnClickListener(v->{settingsExpanded=!settingsExpanded;settings.setVisibility(settingsExpanded?View.VISIBLE:View.GONE);});
        page.addView(controls,marginParams(-1,-2,0,0,0,16));page.addView(settings,new LinearLayout.LayoutParams(-1,-2));
    }

    private void addPageHeader(LinearLayout page,int eyebrow,int title,int subtitle) {
        TextView label=text(getString(eyebrow),11,SECONDARY); label.setLetterSpacing(.13f);
        page.addView(label,marginParams(-1,-2,0,0,0,10));
        TextView heading=text(getString(title),32,TEXT); heading.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        heading.setAccessibilityHeading(true); heading.setLineSpacing(dp(2),1f);
        page.addView(heading,marginParams(-1,-2,0,0,0,8));
        page.addView(text(getString(subtitle),15,SECONDARY),marginParams(-1,-2,0,0,0,24));
    }

    private void addStatusCard(LinearLayout page) {
        LinearLayout card=card();
        TextView title=text(getString(R.string.ui_device_access),12,SECONDARY);title.setLetterSpacing(.08f);
        card.addView(title,marginParams(-1,-2,0,0,0,12));
        serviceStatus=text(getString(R.string.checking),22,TEXT);
        serviceStatus.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        serviceStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        card.addView(serviceStatus,marginParams(-1,-2,0,0,0,8));
        card.addView(text(getString(R.string.ui_service_help),14,SECONDARY),marginParams(-1,-2,0,0,0,18));
        serviceButton=primaryButton(getString(R.string.ui_enable_service));serviceButton.setOnClickListener(v -> openAccessibilitySettings());
        card.addView(serviceButton,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout help=new LinearLayout(this);help.setOrientation(LinearLayout.VERTICAL);help.setVisibility(View.GONE);
        help.addView(text(getString(R.string.restricted_settings_help),13,SECONDARY),marginParams(-1,-2,0,12,0,12));
        Button details=secondaryButton(getString(R.string.open_app_details));details.setOnClickListener(v -> openAppDetails());help.addView(details,new LinearLayout.LayoutParams(-1,-2));
        Button explain=secondaryButton(getString(R.string.ui_permission_help));
        explain.setOnClickListener(v -> {boolean open=help.getVisibility()!=View.VISIBLE;help.setVisibility(open?View.VISIBLE:View.GONE);explain.setText(open?R.string.ui_hide_help:R.string.ui_permission_help);});
        card.addView(explain,marginParams(-1,-2,0,8,0,0));card.addView(help,new LinearLayout.LayoutParams(-1,-2));
        page.addView(card,marginParams(-1,-2,0,0,0,16));
    }

    private void addLanguageCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.language_title, R.string.language_description);
        Spinner spinner = new Spinner(this);
        spinner.setMinimumHeight(dp(48));
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
        card.addView(spinner, new LinearLayout.LayoutParams(-1, -2));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addThemeCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.theme_title, R.string.theme_description);
        Spinner spinner = new Spinner(this);
        spinner.setMinimumHeight(dp(48));
        String[] modes = {ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK};
        String[] labels = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        spinner.setAdapter(styledAdapter(labels));
        String current = ThemeMode.normalize(prefs.getString(ThemeMode.KEY, ThemeMode.SYSTEM));
        spinner.setSelection(ThemeMode.LIGHT.equals(current) ? 1 : ThemeMode.DARK.equals(current) ? 2 : 0);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = modes[position];
                if (!selected.equals(ThemeMode.normalize(prefs.getString(ThemeMode.KEY, ThemeMode.SYSTEM)))) {
                    prefs.edit().putString(ThemeMode.KEY, selected).apply();
                    recreate();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        card.addView(spinner, new LinearLayout.LayoutParams(-1, -2));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addMasterCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.master_title, R.string.master_description);
        Switch master = new Switch(this);
        master.setMinHeight(dp(48));
        master.setText(getString(R.string.master_switch));
        master.setTextColor(TEXT);
        master.setTextSize(14);
        master.setChecked(prefs.getBoolean(AppConfig.MASTER_ENABLED, true));
        master.setOnCheckedChangeListener((buttonView, checked) -> {
            prefs.edit().putBoolean(AppConfig.MASTER_ENABLED, checked).apply();
            updateServiceState();
        });
        card.addView(master, new LinearLayout.LayoutParams(-1, -2));
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
        heading.addView(toggle, new LinearLayout.LayoutParams(dp(88), -2));
        card.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        card.addView(text(getString(descriptionRes), 12, SECONDARY), marginParams(-1, -2, 0, 6, 0, 12));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        card.addView(content, new LinearLayout.LayoutParams(-1, -2));
        boolean[] built = {false};
        Runnable buildRows = () -> {
            if (built[0]) return;
            built[0] = true;
            for (GestureSpec spec : specs) addGestureRow(content, spec);
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

    private void addGestureRow(LinearLayout parent, GestureSpec spec) {
        boolean enabled=prefs.getBoolean(AppConfig.enabledKey(spec.key),false);
        String current=prefs.getString(spec.key,AppConfig.NONE);int index=0;
        for(int i=0;i<AppConfig.ACTION_KEYS.length;i++)if(AppConfig.ACTION_KEYS[i].equals(current)){index=i;break;}
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(16),dp(16),dp(16),dp(16));row.setMinimumHeight(dp(72));
        row.setBackground(buttonBackground(SURFACE_VARIANT,SURFACE_VARIANT));row.setClickable(true);row.setFocusable(true);
        TextView title=text(getString(spec.title),16,TEXT);title.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        row.addView(title,new LinearLayout.LayoutParams(-1,-2));
        String summary=getString(enabled?R.string.ui_binding_on:R.string.ui_binding_off,actionLabels[index]);
        row.addView(text(summary,13,SECONDARY),marginParams(-1,-2,0,6,0,0));
        row.setContentDescription(getString(spec.title)+", "+summary+", "+getString(R.string.ui_edit_binding));
        row.setOnClickListener(v -> {editingGesture=spec;showTab(1);});
        parent.addView(row,marginParams(-1,-2,0,8,0,0));
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
        card.addView(widthBar, marginParams(-1, -2, 0, 0, 0, 4));
        Switch haptic = new Switch(this);
        haptic.setMinHeight(dp(48));
        haptic.setText(getString(R.string.haptic_feedback));
        haptic.setTextColor(TEXT);
        haptic.setTextSize(14);
        haptic.setChecked(prefs.getBoolean(AppConfig.HAPTIC, true));
        haptic.setOnCheckedChangeListener((buttonView, checked) -> prefs.edit().putBoolean(AppConfig.HAPTIC, checked).apply());
        card.addView(haptic, new LinearLayout.LayoutParams(-1, -2));
        TextView resetTitle = text(getString(R.string.reset_title), 15, TEXT);
        resetTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(resetTitle, marginParams(-1, -2, 0, 12, 0, 4));
        card.addView(text(getString(R.string.reset_description), 12, SECONDARY), marginParams(-1, -2, 0, 0, 0, 8));
        Button reset = secondaryButton(getString(R.string.reset_button));
        reset.setOnClickListener(v -> confirmReset());
        card.addView(reset, new LinearLayout.LayoutParams(-1, -2));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addBackupCard(LinearLayout page) {
        LinearLayout card = card();
        addSectionTitle(card, R.string.backup_title, R.string.backup_description);
        Button export = secondaryButton(getString(R.string.backup_export));
        export.setOnClickListener(v -> openBackupPicker(true));
        card.addView(export, new LinearLayout.LayoutParams(-1, -2));
        Button restore = secondaryButton(getString(R.string.backup_import));
        restore.setOnClickListener(v -> openBackupPicker(false));
        card.addView(restore, marginParams(-1, -2, 0, 8, 0, 0));
        page.addView(card, marginParams(-1, -2, 0, 0, 0, 16));
    }

    private void addProfilesCard(LinearLayout page) {
        LinearLayout card=card();addSectionTitle(card,R.string.ui_saved_profiles,R.string.profiles_description);
        saveProfile=primaryButton(getString(R.string.profiles_save));saveProfile.setOnClickListener(v -> nameProfile());
        card.addView(saveProfile,new LinearLayout.LayoutParams(-1,-2));
        profileRows=new LinearLayout(this);profileRows.setOrientation(LinearLayout.VERTICAL);
        card.addView(profileRows,marginParams(-1,-2,0,20,0,20));
        billingStatus=text("",13,SECONDARY);billingStatus.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        card.addView(billingStatus,marginParams(-1,-2,0,0,0,12));
        buyProfiles=primaryButton(getString(R.string.profiles_buy));buyProfiles.setOnClickListener(v -> billing.purchase());
        card.addView(buyProfiles,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout recovery=new LinearLayout(this);recovery.setGravity(Gravity.CENTER_VERTICAL);
        refreshPurchases=secondaryButton(getString(R.string.ui_refresh));refreshPurchases.setOnClickListener(v -> billing.refresh());
        restorePurchases=secondaryButton(getString(R.string.ui_restore));restorePurchases.setOnClickListener(v -> billing.restore());
        LinearLayout.LayoutParams l=new LinearLayout.LayoutParams(0,-2,1);l.setMargins(0,0,dp(4),0);recovery.addView(refreshPurchases,l);
        l=new LinearLayout.LayoutParams(0,-2,1);l.setMargins(dp(4),0,0,0);recovery.addView(restorePurchases,l);
        card.addView(recovery,marginParams(-1,-2,0,8,0,0));page.addView(card,marginParams(-1,-2,0,0,0,16));renderProfiles();
    }

    private void renderProfiles() {
        if (billingStatus == null || isFinishing() || isDestroyed()) return;
        int status;
        switch (billing.phase()) {
            case LOADING: status = R.string.profiles_loading; break;
            case ACTIVE: status = R.string.profiles_active; break;
            case PURCHASING: status = R.string.profiles_purchasing; break;
            case RESTORING: status = R.string.profiles_restoring; break;
            case CANCELLED: status = R.string.profiles_cancelled; break;
            case ERROR: status = R.string.profiles_error; break;
            case LOCKED: status = billing.failure() == BillingGateway.Failure.NO_PRODUCTS
                    ? R.string.profiles_no_products : R.string.profiles_locked; break;
            default: status = R.string.profiles_unconfigured;
        }
        billingStatus.setText(status);
        BillingGateway.Product product = billing.product();
        buyProfiles.setText(product == null ? getString(R.string.profiles_buy)
                : getString(R.string.profiles_product, product.title, product.price));
        buyProfiles.setEnabled(billing.canPurchase());
        refreshPurchases.setEnabled(!billing.busy());
        restorePurchases.setEnabled(!billing.busy());
        List<ProfileStore.Profile> saved = profiles.list();
        saveProfile.setEnabled(billing.canUseProfiles() && saved.size() < ProfileStore.MAX_PROFILES);
        profileRows.removeAllViews();
        if (saved.isEmpty()) {
            LinearLayout empty=new LinearLayout(this);empty.setOrientation(LinearLayout.VERTICAL);
            empty.setPadding(dp(16),dp(22),dp(16),dp(22));empty.setBackground(roundRect(SURFACE_VARIANT,14,0,Color.TRANSPARENT));
            TextView title=text(getString(R.string.profiles_empty),16,TEXT);title.setTypeface(null,android.graphics.Typeface.BOLD);
            empty.addView(title,marginParams(-1,-2,0,0,0,8));
            empty.addView(text(getString(R.string.ui_no_profiles_hint),13,SECONDARY),new LinearLayout.LayoutParams(-1,-2));
            profileRows.addView(empty,new LinearLayout.LayoutParams(-1,-2));
        }
        for (ProfileStore.Profile profile : saved) {
            Button select = secondaryButton(getString(R.string.profiles_switch, profile.name));
            select.setEnabled(billing.canUseProfiles());
            select.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.profiles_switch, profile.name))
                    .setMessage(R.string.profiles_switch_help)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        try { profiles.switchTo(profile.slot, prefs); recreate(); }
                        catch (IOException | IllegalArgumentException | IllegalStateException e) { profileError(); }
                    }).show());
            profileRows.addView(select, marginParams(-1, -2, 0, 8, 0, 0));
            Button delete = secondaryButton(getString(R.string.profiles_delete, profile.name));
            delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.profiles_delete, profile.name))
                    .setMessage(R.string.profiles_delete_help)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        try { profiles.delete(profile.slot); renderProfiles(); }
                        catch (IOException e) { profileError(); }
                    }).show());
            profileRows.addView(delete, marginParams(-1, -2, 0, 4, 0, 0));
        }
    }

    private void nameProfile() {
        EditText name = new EditText(this);
        name.setSingleLine(true);
        name.setHint(R.string.profiles_name);
        name.setFilters(new android.text.InputFilter[] {new android.text.InputFilter.LengthFilter(32)});
        new AlertDialog.Builder(this).setTitle(R.string.profiles_save).setView(name)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    try {
                        profiles.save(name.getText().toString(), prefs);
                        renderProfiles();
                        Toast.makeText(this, R.string.profiles_saved, Toast.LENGTH_SHORT).show();
                    } catch (IOException | IllegalArgumentException | IllegalStateException e) { profileError(); }
                }).show();
    }

    private void profileError() {
        Toast.makeText(this, R.string.profiles_local_error, Toast.LENGTH_LONG).show();
    }

    @SuppressWarnings("deprecation")
    private void openBackupPicker(boolean export) {
        Intent intent = new Intent(export ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(export ? "text/plain" : "*/*");
        if (export) intent.putExtra(Intent.EXTRA_TITLE, "PixelGesture.pgbackup");
        try {
            startActivityForResult(intent, export ? EXPORT_SETTINGS : IMPORT_SETTINGS);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.backup_no_picker, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode != EXPORT_SETTINGS && requestCode != IMPORT_SETTINGS)
                || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        Map<String, ?> snapshot = prefs.getAll();
        appListExecutor.execute(() -> {
            try {
                if (requestCode == EXPORT_SETTINGS) {
                    try (OutputStream output = getContentResolver().openOutputStream(uri, "wt")) {
                        if (output == null) throw new IOException("No output stream");
                        ConfigBackup.write(snapshot, output);
                    }
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            Toast.makeText(this, R.string.backup_export_done, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Map<String, Object> restored;
                    try (InputStream input = getContentResolver().openInputStream(uri)) {
                        restored = ConfigBackup.read(input);
                    }
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) confirmRestore(restored);
                    });
                }
            } catch (IOException | SecurityException e) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Toast.makeText(this, R.string.backup_failed, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void confirmRestore(Map<String, Object> restored) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.backup_confirm_title)
                .setMessage(R.string.backup_confirm_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.backup_import, (dialog, which) -> {
                    ConfigBackup.restore(prefs, restored);
                    Toast.makeText(this, R.string.backup_import_done, Toast.LENGTH_LONG).show();
                    recreate();
                }).show();
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
        enabled.setContentDescription(getString(R.string.ui_enable_binding,getString(spec.title)));
        enabled.setChecked(prefs.getBoolean(AppConfig.enabledKey(spec.key), false));
        enabled.setOnCheckedChangeListener((buttonView, checked) ->
                prefs.edit().putBoolean(AppConfig.enabledKey(spec.key), checked).apply());
        heading.addView(enabled, new LinearLayout.LayoutParams(dp(56), dp(48)));
        row.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        row.addView(text(getString(spec.description), 12, SECONDARY), marginParams(-1, -2, 0, 6, 0, 6));

        Spinner spinner = new Spinner(this);
        spinner.setMinimumHeight(dp(48));
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
        row.addView(spinner, new LinearLayout.LayoutParams(-1, -2));
        row.addView(appButton, marginParams(-1, -2, 0, 6, 0, 0));
        parent.addView(row, marginParams(-1, -2, 0, first ? 4 : 6, 0, 0));
    }

    private ArrayAdapter<String> styledAdapter(String[] labels) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, labels) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(TEXT);
                view.setTextSize(16);
                view.setSingleLine(false);
                view.setMaxLines(3);
                view.setPadding(dp(12),dp(12),dp(12),dp(12));
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
        title.setAccessibilityHeading(true);
        parent.addView(title, marginParams(-1, -2, 0, 0, 0, 4));
        parent.addView(text(getString(descriptionRes), 12, SECONDARY), marginParams(-1, -2, 0, 0, 0, 12));
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(20), dp(20), dp(20));
        card.setBackground(roundRect(SURFACE, darkTheme?24:20, 1, darkTheme?Color.rgb(55,52,61):OUTLINE));
        return card;
    }

    private Button primaryButton(String label) {
        Button button = new Button(this);
        button.setMinHeight(dp(48));
        button.setText(label);
        if(!darkTheme){button.setStateListAnimator(null);button.setElevation(0);}
        button.setTextColor(new ColorStateList(new int[][] {new int[] {-android.R.attr.state_enabled},new int[] {}},new int[] {SECONDARY,ON_PRIMARY}));
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(null, android.graphics.Typeface.BOLD);
        button.setBackground(buttonBackground(PRIMARY,SURFACE_VARIANT));
        button.setPadding(dp(16),dp(12),dp(16),dp(12));
        return button;
    }

    private Button secondaryButton(String label) {
        Button button = new Button(this);
        button.setMinHeight(dp(48));
        button.setText(label);
        if(!darkTheme){button.setStateListAnimator(null);button.setElevation(0);}
        button.setTextColor(new ColorStateList(new int[][] {new int[] {-android.R.attr.state_enabled},new int[] {}},new int[] {SECONDARY,TEXT}));
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setBackground(buttonBackground(SURFACE_VARIANT,SURFACE_VARIANT));
        button.setPadding(dp(16),dp(12),dp(16),dp(12));
        return button;
    }

    private TextView text(String value, int sizeSp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLineSpacing(dp(2),1f);
        return view;
    }

    private Drawable buttonBackground(int normal,int disabled) {
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[] {-android.R.attr.state_enabled},roundRect(disabled,darkTheme?24:14,darkTheme?1:0,Color.rgb(73,69,79)));
        states.addState(new int[] {},roundRect(normal,darkTheme?24:14,darkTheme&&normal!=PRIMARY?1:0,Color.rgb(73,69,79)));
        return new RippleDrawable(ColorStateList.valueOf((PRIMARY & 0x00ffffff) | 0x33000000),states,null);
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
        boolean paused = !prefs.getBoolean(AppConfig.MASTER_ENABLED, true);
        serviceStatus.setText(!enabled ? R.string.ui_status_off
                : paused ? R.string.ui_status_paused : R.string.ui_status_on);
        serviceStatus.setTextColor(TEXT);
        if(serviceDetailStatus!=null){serviceDetailStatus.setText(serviceStatus.getText());serviceDetailStatus.setTextColor(TEXT);}
        if(serviceButton!=null)serviceButton.setText(enabled ? R.string.ui_manage_service : R.string.ui_enable_service);
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
