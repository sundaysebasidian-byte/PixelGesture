package com.pixelgesture;

/** Ephemeral entitlement state. No premium flags are saved in preferences or backups. */
final class BillingController {
    enum Phase { NOT_CONFIGURED, LOADING, LOCKED, ACTIVE, PURCHASING, RESTORING, CANCELLED, ERROR }
    private final BillingGateway gateway;
    private final Runnable changed;
    private int generation;
    private boolean active;
    private BillingGateway.Product product;
    private BillingGateway.Failure failure;
    private Phase phase = Phase.NOT_CONFIGURED;
    private boolean closed;

    BillingController(BillingGateway gateway, Runnable changed) {
        this.gateway = gateway; this.changed = changed;
        gateway.listen(new BillingGateway.Callback() {
            @Override public void success(BillingGateway.Snapshot snapshot) {
                if (closed || busy() || snapshot == null) return;
                active = snapshot.profilesEntitlementActive;
                product = snapshot.product;
                failure = !active && product == null ? BillingGateway.Failure.NO_PRODUCTS : null;
                phase = active ? Phase.ACTIVE : Phase.LOCKED;
                changed.run();
            }
            @Override public void failure(BillingGateway.Failure reason) {
                if (closed || busy()) return;
                active = false; failure = reason; phase = Phase.ERROR; changed.run();
            }
        });
    }
    Phase phase() { return phase; }
    BillingGateway.Failure failure() { return failure; }
    BillingGateway.Product product() { return product; }
    boolean busy() {
        return phase == Phase.LOADING || phase == Phase.PURCHASING || phase == Phase.RESTORING;
    }
    boolean canUseProfiles() { return !closed && active && !busy(); }
    boolean canPurchase() { return !closed && product != null && !busy() && !active; }
    void refresh() {
        if (closed) return;
        active = false; product = null;
        gateway.refresh(begin(Phase.LOADING));
    }
    void purchase() {
        if (closed) return;
        if (!canPurchase()) return;
        gateway.purchase(product.id, begin(Phase.PURCHASING));
    }
    void restore() {
        if (closed) return;
        if (busy()) return;
        active = false;
        gateway.restore(begin(Phase.RESTORING));
    }
    void close() { closed = true; ++generation; active = false; gateway.close(); }
    private BillingGateway.Callback begin(Phase next) {
        int request = ++generation;
        phase = next; failure = null; changed.run();
        return new BillingGateway.Callback() {
            private boolean completed;
            @Override public void success(BillingGateway.Snapshot snapshot) {
                if (closed || request != generation || completed) return;
                completed = true;
                if (snapshot == null) {
                    active = false; phase = Phase.ERROR; failure = BillingGateway.Failure.NETWORK;
                } else {
                    active = snapshot.profilesEntitlementActive;
                    product = snapshot.product;
                    phase = active ? Phase.ACTIVE : Phase.LOCKED;
                    if (!active && product == null) failure = BillingGateway.Failure.NO_PRODUCTS;
                }
                changed.run();
            }
            @Override public void failure(BillingGateway.Failure reason) {
                if (closed || request != generation || completed) return;
                completed = true;
                active = false;
                failure = reason;
                phase = reason == BillingGateway.Failure.NOT_CONFIGURED ? Phase.NOT_CONFIGURED
                        : reason == BillingGateway.Failure.CANCELLED ? Phase.CANCELLED : Phase.ERROR;
                changed.run();
            }
        };
    }
}
