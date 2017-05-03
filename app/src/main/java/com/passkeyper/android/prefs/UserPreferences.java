package com.passkeyper.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.passkeyper.android.adapter.EntryAdapter;

/**
 * CLass used to access user settings related to basic preferences.
 */
public class UserPreferences {

    /* instance of UserPreferences */
    private static UserPreferences instance;
    /* table name */
    private static final String PREFERENCE_NAME = "UserPrefs";
    /* variable names */
    private static final String PREF_SORT_ORDER = "PrefSortOrder";

    private final SharedPreferences preferences;

    private UserPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * @return the SortOrder the user has chosen.
     */
    public EntryAdapter.SortOrder getSortOrder() {
        int i = preferences.getInt(PREF_SORT_ORDER, 0);
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
        SharedPreferences.Editor editor = preferences.edit();
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
