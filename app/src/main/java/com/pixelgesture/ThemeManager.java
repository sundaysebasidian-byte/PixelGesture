package com.pixelgesture;

import android.content.Context;
import android.content.res.Configuration;

/** An app-only visual override. Does not change system theme or gesture preferences. */
public final class ThemeManager {
    private ThemeManager() { }
    public static Context wrap(Context localizedBase) {
        String mode = ThemeMode.normalize(localizedBase.getSharedPreferences(AppConfig.PREFS,
                Context.MODE_PRIVATE).getString(ThemeMode.KEY, ThemeMode.SYSTEM));
        Configuration config = new Configuration(localizedBase.getResources().getConfiguration());
        int system = localizedBase.getApplicationContext().getResources().getConfiguration().uiMode;
        config.uiMode = ThemeMode.apply(mode, config.uiMode, system);
        return localizedBase.createConfigurationContext(config);
    }
}
