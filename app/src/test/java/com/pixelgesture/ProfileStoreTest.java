package com.pixelgesture;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public final class ProfileStoreTest {
    private final BillingControllerTest.Fake gateway = new BillingControllerTest.Fake();
    private final BillingController billing = new BillingController(gateway, () -> {});
    private final TestPreferences storage = new TestPreferences();
    private final TestPreferences live = new TestPreferences();
    private final ProfileStore profiles = new ProfileStore(storage, billing);
    private void unlock() { AppConfig.ensureDefaults(live); billing.refresh(); gateway.reply(true); }

    @Test public void lockedSaveAndSwitchDoNotTouchPreferences() {
        AppConfig.ensureDefaults(live);
        assertThrows(IllegalStateException.class, () -> profiles.save("Study", live));
        assertThrows(IllegalStateException.class, () -> profiles.switchTo(0, live));
        assertTrue(storage.getAll().isEmpty());
    }
    @Test public void twoProfilesPreserveBindingsAcrossRestartAndSwitchPauses() throws IOException {
        unlock(); live.edit().putInt(AppConfig.EDGE_WIDTH, 24).apply(); profiles.save("Study", live);
        live.edit().putInt(AppConfig.EDGE_WIDTH, 36).putBoolean(AppConfig.MASTER_ENABLED, true).apply();
        profiles.save("Travel", live);
        ProfileStore restarted = new ProfileStore(storage, billing);
        assertEquals(2, restarted.list().size()); restarted.switchTo(0, live);
        assertEquals(24, live.getInt(AppConfig.EDGE_WIDTH, 0)); assertFalse(live.getBoolean(AppConfig.MASTER_ENABLED, true));
        restarted.switchTo(1, live); assertEquals(36, live.getInt(AppConfig.EDGE_WIDTH, 0));
    }
    @Test public void revocationKeepsDataButBlocksSwitchAndAllowsDeletion() throws IOException {
        unlock(); profiles.save("Study", live); billing.refresh(); gateway.reply(false);
        assertEquals(1, profiles.list().size());
        assertThrows(IllegalStateException.class, () -> profiles.switchTo(0, live));
        profiles.delete(0); assertTrue(profiles.list().isEmpty());
    }
    @Test public void rejectsBadNamesDuplicatesAndFullSlotsWithoutOverwriting() throws IOException {
        unlock();
        for (String name : new String[]{"", "  ", "x\ny", "123456789012345678901234567890123"}) {
            assertThrows(IllegalArgumentException.class, () -> profiles.save(name, live));
        }
        profiles.save(" Study ", live);
        assertThrows(IllegalArgumentException.class, () -> profiles.save("study", live));
        for (int i = 1; i < 8; i++) profiles.save("Profile " + i, live);
        assertThrows(IllegalArgumentException.class, () -> profiles.save("Extra", live));
        assertEquals("Study", profiles.list().get(0).name);
    }
    @Test public void corruptProfileDoesNotPartiallyRestore() throws IOException {
        unlock(); profiles.save("Study", live); storage.edit().putString("data_0", "broken").apply();
        java.util.Map<String, ?> before = live.getAll();
        assertThrows(IOException.class, () -> profiles.switchTo(0, live)); assertEquals(before, live.getAll());
    }
    @Test public void invalidSlotRejectedAndDeletedSlotReused() throws IOException {
        unlock(); profiles.save("Study", live);
        assertThrows(IllegalArgumentException.class, () -> profiles.switchTo(8, live));
        assertThrows(IllegalArgumentException.class, () -> profiles.delete(-1));
        profiles.delete(0); profiles.save("Travel", live); assertEquals(0, profiles.list().get(0).slot);
    }
    @Test public void restartNeverInheritsEntitlement() throws IOException {
        unlock(); profiles.save("Study", live);
        BillingController fresh = new BillingController(new BillingGateway.Unconfigured(), () -> {});
        ProfileStore restarted = new ProfileStore(storage, fresh);
        assertEquals(1, restarted.list().size());
        assertThrows(IllegalStateException.class, () -> restarted.switchTo(0, live));
    }
}
