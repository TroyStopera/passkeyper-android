package com.passkeyper.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.passkeyper.android.adapter.EntryAdapter;

/**
 * Class used to access user settings related to basic preferences.
 */
public class UserPrefs {

    /* variable names */
    private static final String PREF_APP_CLOSED_AUTH_TIMEOUT = "ClosedAuthTimeout";
    private static final String PREF_FINGERPRINT_ENABLED = "FingerPrintEnabled";
    private static final String PREF_SORT_ORDER = "PrefSortOrder";
    private static final String PREF_BACKUP_TO_GOOGLE = "BackupToGoogle";

    private final SharedPreferences sharedPreferences;

    public UserPrefs(Context context) {
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
    }

    /**
     * Returns the number of milliseconds after the app closes that the user is signed out.
     *
     * @return the time in milliseconds.
     */
    public long getAppClosedAuthTimeout() {
        return sharedPreferences.getLong(PREF_APP_CLOSED_AUTH_TIMEOUT, 30000);
    }

    /**
     * Sets the number of milliseconds after the app closes that the user is signed out.
     *
     * @param timeout the time in milliseconds.
     */
    public void setAppClosedAuthTimeout(long timeout) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(PREF_APP_CLOSED_AUTH_TIMEOUT, timeout);
        editor.apply();
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
