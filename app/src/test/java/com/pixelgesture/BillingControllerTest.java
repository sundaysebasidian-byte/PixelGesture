package com.pixelgesture;

import org.junit.Test;
import static org.junit.Assert.*;

public final class BillingControllerTest {
    static final BillingGateway.Product PRODUCT = new BillingGateway.Product("synthetic_profiles", "Profiles", "$0 test");
    static final class Fake implements BillingGateway {
        Callback pending;
        Callback observer;
        boolean closed;
        int purchases;
        @Override public void refresh(Callback c) { pending = c; }
        @Override public void purchase(String product, Callback c) { purchases++; pending = c; }
        @Override public void restore(Callback c) { pending = c; }
        @Override public void listen(Callback c) { observer = c; }
        @Override public void close() { closed = true; }
        void reply(boolean active) { pending.success(new Snapshot(active, PRODUCT)); }
    }
    @Test public void unconfiguredNeverUnlocks() {
        BillingController c = new BillingController(new BillingGateway.Unconfigured(), () -> {});
        c.refresh(); c.purchase(); c.restore();
        assertEquals(BillingController.Phase.NOT_CONFIGURED, c.phase());
        assertFalse(c.canUseProfiles()); assertFalse(c.canPurchase());
    }
    @Test public void productLoadingAndPurchaseRequireCustomerInfo() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.purchase(); assertEquals(0, f.purchases);
        c.refresh(); assertTrue(c.busy()); assertFalse(c.canUseProfiles());
        f.reply(false); assertTrue(c.canPurchase());
        c.purchase(); assertEquals(1, f.purchases); assertEquals(BillingController.Phase.PURCHASING, c.phase());
        assertFalse(c.canUseProfiles());
        f.reply(true); assertTrue(c.canUseProfiles()); assertFalse(c.canPurchase());
    }
    @Test public void successfulTransactionWithoutEntitlementStaysLocked() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); f.reply(false); c.purchase(); f.reply(false);
        assertEquals(BillingController.Phase.LOCKED, c.phase()); assertFalse(c.canUseProfiles());
    }
    @Test public void cancellationAndFailureNeverUnlockAndAllowRetry() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); f.reply(false); c.purchase(); f.pending.failure(BillingGateway.Failure.CANCELLED);
        assertEquals(BillingController.Phase.CANCELLED, c.phase()); assertFalse(c.canUseProfiles());
        c.purchase(); f.pending.failure(BillingGateway.Failure.PURCHASE);
        assertEquals(BillingController.Phase.ERROR, c.phase()); assertTrue(c.canPurchase());
    }
    @Test public void restoreChecksActualEntitlementAndRevocationLocks() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.restore(); assertEquals(BillingController.Phase.RESTORING, c.phase()); f.reply(true);
        assertTrue(c.canUseProfiles()); c.refresh(); assertFalse(c.canUseProfiles()); f.reply(false);
        assertFalse(c.canUseProfiles());
    }
    @Test public void failedRefreshAfterAccessFailsClosed() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); f.reply(true); c.refresh(); f.pending.failure(BillingGateway.Failure.NETWORK);
        assertFalse(c.canUseProfiles()); assertNull(c.product());
    }
    @Test public void staleAndDuplicateCallbacksCannotGrantAccess() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); BillingGateway.Callback old = f.pending;
        c.refresh(); f.reply(false); old.success(new BillingGateway.Snapshot(true, PRODUCT));
        assertFalse(c.canUseProfiles()); f.reply(true); assertFalse(c.canUseProfiles());
    }
    @Test public void missingProductsAndMalformedReplyHaveClearState() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); f.pending.success(new BillingGateway.Snapshot(false, null));
        assertEquals(BillingGateway.Failure.NO_PRODUCTS, c.failure()); assertFalse(c.canPurchase());
        c.refresh(); f.pending.success(null);
        assertEquals(BillingController.Phase.ERROR, c.phase()); assertFalse(c.canUseProfiles());
    }
    @Test public void customerInfoUpdatesRevokeAccessAndCannotRacePurchase() {
        Fake f = new Fake(); BillingController c = new BillingController(f, () -> {});
        c.refresh(); f.reply(true);
        f.observer.success(new BillingGateway.Snapshot(false, PRODUCT));
        assertFalse(c.canUseProfiles()); assertTrue(c.canPurchase());
        c.purchase(); f.observer.success(new BillingGateway.Snapshot(true, PRODUCT));
        assertFalse(c.canUseProfiles());
        f.pending.failure(BillingGateway.Failure.CANCELLED);
        assertFalse(c.canUseProfiles());
    }
    @Test public void destroyedControllerIgnoresAllCallbacksAndRequests() {
        Fake f = new Fake(); final int[] updates = {0};
        BillingController c = new BillingController(f, () -> updates[0]++);
        c.refresh(); f.reply(false); c.purchase();
        c.close(); int before = updates[0];
        assertTrue(f.closed);
        f.reply(true); f.observer.success(new BillingGateway.Snapshot(true, PRODUCT));
        f.observer.failure(BillingGateway.Failure.NETWORK);
        c.purchase(); c.restore(); c.refresh();
        assertEquals(before, updates[0]); assertEquals(1, f.purchases);
        assertFalse(c.canUseProfiles()); assertFalse(c.canPurchase());
    }
}
