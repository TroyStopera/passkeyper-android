package com.passkeyper.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class used to access user settings related to authentication preferences.
 */
public class AuthPreferences extends Preferences {

    /* instance of AuthPreferences */
    private static AuthPreferences instance;
    /* variable names */
    private static final String PREF_APP_CLOSED_AUTH_TIMEOUT = "ClosedAuthTimeout";

    private AuthPreferences(Context context) {
        super(context, "AuthPrefs");
    }

    /**
     * Returns the number of milliseconds after the app closes that the user is signed out.
     *
     * @return the time in milliseconds.
     */
    public long getAppClosedAuthTimeout() {
        return prefs().getLong(PREF_APP_CLOSED_AUTH_TIMEOUT, 10000);
    }

    /**
     * Sets the number of milliseconds after the app closes that the user is signed out.
     *
     * @param timeout the time in milliseconds.
     */
    public void setAppClosedAuthTimeout(long timeout) {
        SharedPreferences.Editor editor = edit();
        editor.putLong(PREF_APP_CLOSED_AUTH_TIMEOUT, timeout);
        editor.apply();
    }

    /**
     * Get the instance of AuthPreferences.
     *
     * @param context the Context used to load the SharedPreferences object.
     * @return the instance of AuthPreferences.
     */
    public static AuthPreferences get(Context context) {
        if (instance == null)
            instance = new AuthPreferences(context);
        return instance;
    }

}
