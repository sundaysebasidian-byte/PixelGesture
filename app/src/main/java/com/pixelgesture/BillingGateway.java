package com.pixelgesture;

/** Billing boundary; the debug adapter derives access from real CustomerInfo. */
interface BillingGateway {
    enum Failure { NOT_CONFIGURED, NETWORK, NO_PRODUCTS, CANCELLED, PURCHASE, RESTORE }
    final class Product {
        final String id, title, price;
        Product(String id, String title, String price) {
            this.id = id; this.title = title; this.price = price;
        }
    }
    final class Snapshot {
        final boolean profilesEntitlementActive;
        final Product product;
        Snapshot(boolean active, Product product) {
            this.profilesEntitlementActive = active; this.product = product;
        }
    }
    interface Callback {
        void success(Snapshot customerInfo);
        void failure(Failure reason);
    }
    void refresh(Callback callback);
    void purchase(String productId, Callback callback);
    void restore(Callback callback);
    default void listen(Callback callback) { }
    default void close() { }

    /** Used by release and debug builds without a configured public test key. */
    final class Unconfigured implements BillingGateway {
        @Override public void refresh(Callback callback) { callback.failure(Failure.NOT_CONFIGURED); }
        @Override public void purchase(String productId, Callback callback) { callback.failure(Failure.NOT_CONFIGURED); }
        @Override public void restore(Callback callback) { callback.failure(Failure.NOT_CONFIGURED); }
    }
}
