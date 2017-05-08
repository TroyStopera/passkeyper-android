package com.passkeyper.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Abstract cass used to access user settings.
 */
abstract class Preferences {

    private final SharedPreferences preferences;

    Preferences(Context context, String tableName) {
        preferences = context.getSharedPreferences(tableName, Context.MODE_PRIVATE);
    }

    final SharedPreferences prefs() {
        return preferences;
    }

    SharedPreferences.Editor edit() {
        return prefs().edit();
    }

}
