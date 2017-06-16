package com.passkeyper.android.fragment;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.passkeyper.android.R;
import com.passkeyper.android.UserPreferences;
import com.passkeyper.android.Vault;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.FingerprintAuthHelper;
import com.passkeyper.android.auth.SetupFingerprintDialog;

/**
 * Preference fragment that is used for basic user settings.
 */
public class UserPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, SetupFingerprintDialog.FingerprintSetupListener {

    private SwitchPreferenceCompat fingerprintPref;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.user_preferences, s);

        fingerprintPref = (SwitchPreferenceCompat) findPreference(getString(R.string.prefKey_fingerprint));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || !FingerprintAuthHelper.isAvailable(getContext())) {
            fingerprintPref.setShouldDisableView(true);
            fingerprintPref.setEnabled(false);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        switch (key) {
            case UserPreferences.PREF_FINGERPRINT_ENABLED:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //verify fingerprint when enabled
                    if (prefs.getBoolean(key, false)) {
                        SetupFingerprintDialog setupDialog = new SetupFingerprintDialog();
                        setupDialog.setCancelable(false);
                        setupDialog.setListener(this);
                        setupDialog.setPassword(Vault.get().getManager().getPassword());
                        setupDialog.show(getFragmentManager(), "Setup");
                    }
                    //remove the fingerprint password when disabled
                    else new AuthData(getContext()).clearFingerprintPassword();
                }
                break;
        }
    }

    @Override
    public void onCancelled() {
        fingerprintPref.setChecked(false);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure() {
        fingerprintPref.setChecked(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
