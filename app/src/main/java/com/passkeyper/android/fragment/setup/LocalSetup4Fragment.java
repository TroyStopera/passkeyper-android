package com.passkeyper.android.fragment.setup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.activity.InitialSetupActivity;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.FingerprintAuthHelper;
import com.passkeyper.android.auth.VerifyFingerprintDialog;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.UserPrefs;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vault.local.LocalVaultManager;

import java.util.Arrays;

/**
 * LoginFragment for finishing the setup of the local vault.
 */
public class LocalSetup4Fragment extends AbstractLoginFragment<InitialSetupActivity> implements VerifyFingerprintDialog.FingerprintSetupListener {

    private static final String TAG = "Setup Step 4";

    private TextInputEditText timeout;
    private Switch fingerprintEnabled, backupEnabled;
    private ImageView icon;
    private ProgressBar loading;

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_4_fragment, container, false);

        view.findViewById(R.id.back_btn).setOnClickListener(v -> loginFragmentActivity.pop());
        view.findViewById(R.id.finish_btn).setOnClickListener(v -> {
            char[] pass = loginFragmentActivity.getSetup2Fragment().getPassword();
            if (fingerprintEnabled.isChecked() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                setupFingerprint(pass);
            else
                setupVault(pass);
        });

        timeout = (TextInputEditText) view.findViewById(R.id.timeout_minutes);
        fingerprintEnabled = (Switch) view.findViewById(R.id.fingerprint_enabled_switch);
        backupEnabled = (Switch) view.findViewById(R.id.backup_enabled_switch);
        icon = (ImageView) view.findViewById(R.id.setup_icon);
        loading = (ProgressBar) view.findViewById(R.id.loading);

        timeout.setOnEditorActionListener((v, i, e) -> {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            return true;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && FingerprintAuthHelper.isAvailable(getContext()))
            fingerprintEnabled.setVisibility(View.VISIBLE);
        else
            fingerprintEnabled.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCancelled() {
        Toast.makeText(getContext(), R.string.fingerprint_setup_cancelled, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccess() {
        //now that the fingerprint is done, setup the vault
        setupVault(loginFragmentActivity.getSetup2Fragment().getPassword());
    }

    @Override
    public void onFailure() {
        Toast.makeText(getContext(), R.string.fingerprint_failed, Toast.LENGTH_LONG).show();
    }

    //TODO: determine if this is a potential bug or not
    @SuppressLint("StaticFieldLeak")
    private void setupVault(char[] pass) {
        //must be done async or else UI hangs
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                icon.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                char[] securityAnswer = null;
                try {
                    AuthData authData = new AuthData(getContext());
                    UserPrefs userPrefs = new UserPrefs(getContext());
                    //setup the data needed outside of the database
                    String securityQuestion = loginFragmentActivity.getSetup3Fragment().getQuestion();
                    securityAnswer = loginFragmentActivity.getSetup3Fragment().getAnswer();
                    //start with the encrypted password because that may cause an error
                    authData.setEncryptedPassword(pass, securityQuestion, securityAnswer);
                    authData.setSecurityQuestion(securityQuestion);
                    //save this fragments user preferences
                    userPrefs.setAppClosedAuthTimeout(Long.valueOf(timeout.getText().toString()) * 1000);
                    userPrefs.setFingerprintEnabled(fingerprintEnabled.isChecked());
                    userPrefs.setBackupToGoogleEnabled(backupEnabled.isChecked());
                    //setup and log into database
                    Vault vault = Vault.get();
                    LocalVaultManager.setupLocalDb(getContext(), pass, securityQuestion, securityAnswer);
                    vault.signInToLocalVault(getContext(), pass);
                    //update the recovery data in the database
                    VaultManager.RecoveryData recoveryData = vault.getManager().getRecoveryData();
                    recoveryData.setSecurityQuestion(securityQuestion);
                    recoveryData.setSecurityAnswer(securityAnswer);
                    vault.getManager().updateRecoveryData(recoveryData);
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, "Unable to setup vault", e);
                    return false;
                } finally {
                    Arrays.fill(pass, '\0');
                    if (securityAnswer != null)
                        Arrays.fill(securityAnswer, '\0');
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                loading.setVisibility(View.GONE);
                icon.setVisibility(View.VISIBLE);

                if (success)
                    loginFragmentActivity.redirectAndFinish();
                else {
                    //if unsuccessful, notify the user of the error and try to run setup again
                    Toast.makeText(getContext(), R.string.setup_fatal_error, Toast.LENGTH_LONG).show();
                    loginFragmentActivity.redirectAndFinish();
                }
            }
        }.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setupFingerprint(char[] pass) {
        VerifyFingerprintDialog setupDialog = new VerifyFingerprintDialog();
        setupDialog.setTitle(getString(R.string.fingerprint_setup_title));
        setupDialog.setCancelable(false);
        setupDialog.setListener(this);
        setupDialog.setPassword(pass);
        setupDialog.show(getFragmentManager(), "Setup");
        //clear the password
        Arrays.fill(pass, '\0');
    }

}
