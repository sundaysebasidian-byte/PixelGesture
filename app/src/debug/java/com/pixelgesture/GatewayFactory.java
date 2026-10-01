package com.pixelgesture;
import android.app.Activity;
import com.revenuecat.purchases.Purchases;
final class GatewayFactory {
    static BillingGateway create(Activity activity) {
        return Purchases.isConfigured() ? new RevenueCatGateway(activity, Purchases.getSharedInstance())
                : new BillingGateway.Unconfigured();
    }
}
