package com.pixelgesture;

import android.content.SharedPreferences;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Eight private local slots. Reuses the strict backup schema and pauses gestures on switch. */
final class ProfileStore {
    static final String PREFS = "pixelgesture_nextgen_profiles";
    static final int MAX_PROFILES = 8;
    private final SharedPreferences storage;
    private final BillingController billing;
    static final class Profile {
        final int slot;
        final String name;
        Profile(int slot, String name) { this.slot = slot; this.name = name; }
    }
    ProfileStore(SharedPreferences storage, BillingController billing) {
        this.storage = storage; this.billing = billing;
    }
    List<Profile> list() {
        List<Profile> result = new ArrayList<>();
        for (int slot = 0; slot < MAX_PROFILES; slot++) {
            String name = storage.getString("name_" + slot, null);
            if (name != null) result.add(new Profile(slot, name));
        }
        return result;
    }
    void save(String name, SharedPreferences live) throws IOException {
        requireAccess();
        if (name == null) throw new IllegalArgumentException("Missing profile name");
        name = name.trim();
        if (name.isEmpty() || name.length() > 32) throw new IllegalArgumentException("Invalid profile name");
        for (int i = 0; i < name.length(); i++) {
            if (Character.isISOControl(name.charAt(i))) throw new IllegalArgumentException("Invalid profile name");
        }
        int free = -1;
        for (int slot = 0; slot < MAX_PROFILES; slot++) {
            String existing = storage.getString("name_" + slot, null);
            if (name.equalsIgnoreCase(existing)) throw new IllegalArgumentException("Duplicate profile name");
            if (existing == null && free < 0) free = slot;
        }
        if (free < 0) throw new IllegalArgumentException("Profile limit reached");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ConfigBackup.write(live.getAll(), bytes);
        requireAccess();
        if (!storage.edit().putString("name_" + free, name)
                .putString("data_" + free, bytes.toString("ISO-8859-1")).commit()) {
            throw new IOException("Profile could not be saved");
        }
    }
    void switchTo(int slot, SharedPreferences live) throws IOException {
        requireAccess(); checkSlot(slot);
        String data = storage.getString("data_" + slot, null);
        if (data == null) throw new IOException("Missing profile");
        java.util.Map<String, Object> values = ConfigBackup.read(new ByteArrayInputStream(
                data.getBytes(StandardCharsets.ISO_8859_1)));
        requireAccess();
        ConfigBackup.restore(live, values);
    }
    void delete(int slot) throws IOException {
        // Deleting one's local saved data never requires a purchase.
        checkSlot(slot);
        if (!storage.edit().remove("name_" + slot).remove("data_" + slot).commit()) {
            throw new IOException("Profile could not be deleted");
        }
    }
    private void requireAccess() {
        if (!billing.canUseProfiles()) throw new IllegalStateException("Verified entitlement required");
    }
    private static void checkSlot(int slot) {
        if (slot < 0 || slot >= MAX_PROFILES) throw new IllegalArgumentException("Invalid profile slot");
    }
}
