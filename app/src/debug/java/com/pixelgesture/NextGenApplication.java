package com.pixelgesture;

import android.app.Application;
import android.content.SharedPreferences;
import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import java.util.UUID;

/** Test-only boot configuration; never compiled into the release variant. */
public final class NextGenApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG || !BuildConfig.RC_TEST_KEY.startsWith("test_")) return;
        SharedPreferences synthetic = getSharedPreferences("nextgen_synthetic_customer", MODE_PRIVATE);
        String id = synthetic.getString("test_customer", null);
        if (id == null) {
            id = "pixelgesture-test-" + UUID.randomUUID();
            synthetic.edit().putString("test_customer", id).apply();
        }
        Purchases.setLogLevel(LogLevel.ERROR);
        // SDK 9.9.0 selects Test Store from the test_ key. Passing TEST_STORE to
        // Builder.store crashes its attribution factory; keep the Android default.
        // RevenueCatGateway checks the resulting runtime store before any action.
        Purchases.configure(new PurchasesConfiguration.Builder(this, BuildConfig.RC_TEST_KEY)
                .appUserID(id)
                .automaticDeviceIdentifierCollectionEnabled(false)
                .diagnosticsEnabled(false).showInAppMessagesAutomatically(false).build());
    }
}
