package com.passkeyper.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.passkeyper.android.adapter.EntryAdapter;

/**
 * Class used to access user settings related to basic preferences.
 */
public class UserPreferences extends Preferences {

    /* instance of UserPreferences */
    private static UserPreferences instance;
    /* variable names */
    private static final String PREF_SORT_ORDER = "PrefSortOrder";
    private static final String PREF_BACKUP_TO_GOOGLE = "BackupToGoogle";

    private UserPreferences(Context context) {
        super(context, "UserPrefs");
    }

    /**
     * @return the SortOrder the user has chosen.
     */
    public EntryAdapter.SortOrder getSortOrder() {
        int i = prefs().getInt(PREF_SORT_ORDER, 0);
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
        SharedPreferences.Editor editor = edit();
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
        return prefs().getBoolean(PREF_BACKUP_TO_GOOGLE, false);
    }

    /**
     * Set whether the app data should be backed up to Google.
     *
     * @param enabled true if the app data should be backed up.
     */
    public void setBackupToGoogleEnabled(boolean enabled) {
        SharedPreferences.Editor editor = edit();
        editor.putBoolean(PREF_BACKUP_TO_GOOGLE, enabled);
        editor.apply();
    }

    /**
     * Get the instance of UserPreferences.
     *
     * @param context the Context used to load the SharedPreferences object.
     * @return the instance of UserPreferences.
     */
    public static UserPreferences get(Context context) {
        if (instance == null)
            instance = new UserPreferences(context);
        return instance;
    }

}
