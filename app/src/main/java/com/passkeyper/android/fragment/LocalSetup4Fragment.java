package com.passkeyper.android.fragment;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Switch;
import android.widget.TextView;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.activity.InitialSetupActivity;
import com.passkeyper.android.prefs.AuthPreferences;
import com.passkeyper.android.prefs.UserPreferences;
import com.passkeyper.android.vault.local.LocalVaultManager;

import java.util.Arrays;

/**
 * LoginFragment for finishing the setup of the local vault.
 */
public class LocalSetup4Fragment extends AbstractLoginFragment<InitialSetupActivity> {

    private TextInputEditText timeout;
    private Switch fingerprintEnabled, backupEnabled;

    @Override
    View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_4_fragment, container, false);

        view.findViewById(R.id.back_btn).setOnClickListener(v -> loginFragmentActivity.pop());
        view.findViewById(R.id.finish_btn).setOnClickListener(v -> finish());

        timeout = (TextInputEditText) view.findViewById(R.id.timeout_minutes);
        fingerprintEnabled = (Switch) view.findViewById(R.id.fingerprint_enabled_switch);
        backupEnabled = (Switch) view.findViewById(R.id.backup_enabled_switch);

        timeout.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });

        if (isFingerprintAvailable()) fingerprintEnabled.setVisibility(View.VISIBLE);

        return view;
    }

    private void finish() {
        try {
            AuthPreferences authPreferences = AuthPreferences.get(getContext());
            char[] pass = loginFragmentActivity.getSetup2Fragment().getPassword();
            //setup the data needed outside of the database
            String securityQuestion = loginFragmentActivity.getSetup3Fragment().getQuestion();
            char[] securityAnswer = loginFragmentActivity.getSetup3Fragment().getAnswer();
            //start with the encrypted password because that may cause a crash then do question
            authPreferences.setEncryptedPassword(pass, securityQuestion, securityAnswer);
            authPreferences.setSecurityQuestion(securityQuestion);
            //save this fragments settings
            authPreferences.setAppClosedAuthTimeout(Long.valueOf(timeout.getText().toString()) * 1000);
            authPreferences.setFingerprintEnabled(fingerprintEnabled.isEnabled());
            UserPreferences.get(getContext()).setBackupToGoogleEnabled(backupEnabled.isEnabled());
            //setup and log into database
            LocalVaultManager.setupLocalDb(getContext(), pass, securityQuestion, securityAnswer);
            Vault.get().signInToLocalVault(getContext(), pass);
            //clear password and finish
            Arrays.fill(pass, '\0');
            loginFragmentActivity.redirectAndFinish();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save encrypted password for recovery", e);
        }
    }

    private boolean isFingerprintAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) getContext().getSystemService(Context.FINGERPRINT_SERVICE);
            return fingerprintManager.isHardwareDetected();
        }
        return false;
    }

}
