package com.pixelgesture;

import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;

import java.util.Locale;

/** Applies the user-selected app language independently of the device language. */
public final class LanguageManager {
    private LanguageManager() { }

    public static Context wrap(Context base) {
        SharedPreferences prefs = base.getSharedPreferences(AppConfig.PREFS, Context.MODE_PRIVATE);
        String language = prefs.getString(AppConfig.LANGUAGE, AppConfig.LANGUAGE_ENGLISH);
        Locale locale = localeFor(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return base.createConfigurationContext(configuration);
    }

    public static String getString(Context base, int resourceId) {
        return wrap(base).getString(resourceId);
    }

    public static void setLanguage(Context base, String language) {
        base.getSharedPreferences(AppConfig.PREFS, Context.MODE_PRIVATE)
                .edit().putString(AppConfig.LANGUAGE, language).commit();
        syncPlatformLocale(base, language);
    }

    public static void syncPlatformLocale(Context base) {
        String language = base.getSharedPreferences(AppConfig.PREFS, Context.MODE_PRIVATE)
                .getString(AppConfig.LANGUAGE, AppConfig.LANGUAGE_ENGLISH);
        syncPlatformLocale(base, language);
    }

    private static void syncPlatformLocale(Context base, String language) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        LocaleManager manager = base.getSystemService(LocaleManager.class);
        if (manager == null) return;
        String tag = localeFor(language).toLanguageTag();
        if (!tag.equals(manager.getApplicationLocales().toLanguageTags())) {
            manager.setApplicationLocales(LocaleList.forLanguageTags(tag));
        }
    }

    private static Locale localeFor(String language) {
        return AppConfig.LANGUAGE_CHINESE.equals(language)
                ? Locale.forLanguageTag("zh-CN") : Locale.ENGLISH;
    }
}
