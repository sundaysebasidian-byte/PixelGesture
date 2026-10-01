package com.pixelgesture;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import com.revenuecat.purchases.CacheFetchPolicy;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.EntitlementInfo;
import com.revenuecat.purchases.Offering;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesError;
import com.revenuecat.purchases.Store;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener;
import com.revenuecat.purchases.models.StoreProduct;
import com.revenuecat.purchases.models.StoreTransaction;
import java.lang.ref.WeakReference;

/** Real SDK callbacks only. No local premium state, direct HTTP or fabricated receipt. */
final class RevenueCatGateway implements BillingGateway {
    private final Purchases sdk;
    private final WeakReference<Activity> activity;
    private final Handler main = new Handler(Looper.getMainLooper());
    private com.revenuecat.purchases.Package selected;
    private boolean closed;
    private UpdatedCustomerInfoListener listener;

    RevenueCatGateway(Activity activity, Purchases sdk) {
        this.activity = new WeakReference<>(activity); this.sdk = sdk;
        if (sdk.getStore() != Store.TEST_STORE) throw new IllegalStateException("Only Test Store is permitted");
    }
    @Override public void refresh(Callback callback) {
        selected = null;
        sdk.getOfferings(new ReceiveOfferingsCallback() {
            @Override public void onReceived(Offerings offerings) {
                dispatch(() -> {
                    Offering offering = offerings.getCurrent();
                    if (offering != null) {
                        com.revenuecat.purchases.Package lifetime = offering.getLifetime();
                        if (lifetime != null && lifetime.getProduct().getId().equals("profiles_lifetime_test")) selected = lifetime;
                    }
                    // Access is checked even if the offering is temporarily unavailable.
                    customer(callback, Failure.NETWORK);
                });
            }
            @Override public void onError(PurchasesError error) {
                dispatch(() -> callback.failure(Failure.NETWORK));
            }
        });
    }
    private void customer(Callback callback, Failure failure) {
        sdk.getCustomerInfo(CacheFetchPolicy.FETCH_CURRENT, new ReceiveCustomerInfoCallback() {
            @Override public void onReceived(CustomerInfo info) { dispatch(() -> callback.success(snapshot(info))); }
            @Override public void onError(PurchasesError error) { dispatch(() -> callback.failure(failure)); }
        });
    }
    @Override public void purchase(String productId, Callback callback) {
        Activity owner = activity.get();
        if (selected == null || !selected.getProduct().getId().equals(productId)) {
            callback.failure(Failure.NO_PRODUCTS); return;
        }
        if (owner == null || owner.isFinishing() || owner.isDestroyed()) return;
        sdk.purchase(new PurchaseParams.Builder(owner, selected).build(), new PurchaseCallback() {
            @Override public void onCompleted(StoreTransaction transaction, CustomerInfo info) {
                dispatch(() -> callback.success(snapshot(info)));
            }
            @Override public void onError(PurchasesError error, boolean cancelled) {
                dispatch(() -> callback.failure(cancelled ? Failure.CANCELLED : Failure.PURCHASE));
            }
        });
    }
    @Override public void restore(Callback callback) {
        sdk.restorePurchases(new ReceiveCustomerInfoCallback() {
            // Test Store restore returns the current customer, not real store receipts.
            // Follow it with a fresh fetch so a cached entitlement cannot unlock access.
            @Override public void onReceived(CustomerInfo info) { dispatch(() -> customer(callback, Failure.RESTORE)); }
            @Override public void onError(PurchasesError error) { dispatch(() -> callback.failure(Failure.RESTORE)); }
        });
    }
    private Snapshot snapshot(CustomerInfo info) {
        EntitlementInfo entitlement = info.getEntitlements().getActive().get("profiles");
        // Reject any entitlement not returned as a sandbox Test Store entitlement.
        boolean active = entitlement != null && entitlement.isActive()
                && entitlement.isSandbox() && entitlement.getStore() == Store.TEST_STORE;
        StoreProduct product = selected == null ? null : selected.getProduct();
        return new Snapshot(active, product == null ? null
                : new Product(product.getId(), product.getTitle(), product.getPrice().getFormatted()));
    }
    @Override public void listen(Callback callback) {
        listener = info -> dispatch(() -> callback.success(snapshot(info)));
        sdk.setUpdatedCustomerInfoListener(listener);
    }
    @Override public void close() {
        closed = true; activity.clear();
        if (sdk.getUpdatedCustomerInfoListener() == listener) sdk.removeUpdatedCustomerInfoListener();
        main.removeCallbacksAndMessages(null);
    }
    private void dispatch(Runnable task) {
        main.post(() -> {
            Activity owner = activity.get();
            if (!closed && owner != null && !owner.isFinishing() && !owner.isDestroyed()) task.run();
        });
    }
}
