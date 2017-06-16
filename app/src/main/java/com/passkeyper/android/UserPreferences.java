package com.passkeyper.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.passkeyper.android.adapter.EntryAdapter;

/**
 * Class used to access user settings related to basic preferences.
 */
public class UserPreferences {

    /* prefs shown in preference activity */
    public static final String PREF_FINGERPRINT_ENABLED = "pref_fingerprintEnabled";
    public static final String PREF_BACKUP_TO_GOOGLE = "pref_backupToGoogle";
    /* other prefs  */
    private static final String PREF_SORT_ORDER = "pref_sortOrder";
    private static final String PREF_VAULT_MANAGER_TYPE = "pref_vaultManager";

    private final SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Returns the int that represents which VaultManager the user uses.
     *
     * @return the vault manager type as an int.
     */
    public int getVaultManagerType() {
        return sharedPreferences.getInt(PREF_VAULT_MANAGER_TYPE, Vault.LOCAL_VAULT);
    }

    /**
     * Set the int that represents which VaultManager the user uses.
     *
     * @param type the vault manager type as an int.
     */
    public void setVaultManagerType(int type) {
        sharedPreferences.edit().putInt(PREF_VAULT_MANAGER_TYPE, type).apply();
    }

    /**
     * Returns whether the fingerprint login is enabled.
     *
     * @return true if fingerprint authentication is enabled
     */
    public boolean isFingerprintEnabled() {
        return sharedPreferences.getBoolean(PREF_FINGERPRINT_ENABLED, false);
    }

    /**
     * Sets if fingerprint authentication is enabled.
     *
     * @param enabled whether fingerprint should be enabled;
     */
    public void setFingerprintEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(PREF_FINGERPRINT_ENABLED, enabled).apply();
    }

    /**
     * Returns the user's preferred sort order.
     *
     * @return the SortOrder the user has chosen.
     */
    public EntryAdapter.SortOrder getSortOrder() {
        int i = sharedPreferences.getInt(PREF_SORT_ORDER, 0);
        switch (i) {
            case 0:
                return EntryAdapter.SortOrder.AtoZ;
            case 1:
                return EntryAdapter.SortOrder.ZtoA;
            case 2:
                return EntryAdapter.SortOrder.OldestFirst;
            case 3:
                return EntryAdapter.SortOrder.NewestFirst;
            default:
                return EntryAdapter.SortOrder.AtoZ;
        }
    }

    /**
     * Set the user's preferred SortOrder.
     *
     * @param order the SortOrder to use.
     */
    public void setSortOrder(EntryAdapter.SortOrder order) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (order) {
            case AtoZ:
                editor.putInt(PREF_SORT_ORDER, 0);
                break;
            case ZtoA:
                editor.putInt(PREF_SORT_ORDER, 1);
                break;
            case OldestFirst:
                editor.putInt(PREF_SORT_ORDER, 2);
                break;
            case NewestFirst:
                editor.putInt(PREF_SORT_ORDER, 3);
                break;
        }
        editor.apply();
    }

    /**
     * Returns whether the user wants app data backed up to Google.
     *
     * @return true if backup is enabled.
     */
    public boolean isBackupToGoogleEnabled() {
        return sharedPreferences.getBoolean(PREF_BACKUP_TO_GOOGLE, false);
    }

    /**
     * Set whether the app data should be backed up to Google.
     *
     * @param enabled true if the app data should be backed up.
     */
    public void setBackupToGoogleEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREF_BACKUP_TO_GOOGLE, enabled);
        editor.apply();
    }

}
