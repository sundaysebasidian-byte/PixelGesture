package com.pixelgesture;
import android.app.Activity;
/** Release cannot load the test key or RevenueCat test SDK. */
final class GatewayFactory {
    static BillingGateway create(Activity activity) { return new BillingGateway.Unconfigured(); }
}
