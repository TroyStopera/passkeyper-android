package com.passkeyper.android.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.passkeyper.android.Vault;
import com.passkeyper.android.fragment.UserPreferenceFragment;

/**
 * Activity that houses the preference fragment.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //the vault manager must be signed in in order to change passwords
        if (!Vault.get().hasManager() || Vault.get().getManager().isClosed()) {
            Log.wtf("Settings", "Settings activity was loaded without signed in user");
            finish();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new UserPreferenceFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Vault vault = Vault.get();
        //if not signed in then request sign in
        if (!vault.hasManager() && !vault.loadManager())
            vault.requestSignIn(this, SettingsActivity.class);
    }
}
