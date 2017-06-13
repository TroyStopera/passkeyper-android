package com.passkeyper.android.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.UserPrefs;
import com.passkeyper.android.Vault;

import java.util.Arrays;

/**
 * Class that encapsulates all logic for updating a user's password.
 */
public class ResetPasswordHelper implements VerifyFingerprintDialog.FingerprintSetupListener {

    private static final String TAG = "Reset Password";

    private final Context context;
    private final String oldEncryptedPass;
    private final FragmentManager fragmentManager;

    private char[] oldPass, newPass, answer;
    private ResetPasswordListener resetPasswordListener;
    private boolean wasFingerprintUpdated = false;

    public ResetPasswordHelper(Context context, FragmentManager fragmentManager) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        oldEncryptedPass = new AuthData(context).getEncryptedFingerprintPass();
    }

    public void setResetPasswordListener(ResetPasswordListener resetPasswordListener) {
        this.resetPasswordListener = resetPasswordListener;
    }

    @SuppressLint("StaticFieldLeak")
    public void reset(char[] newPass, char[] oldPass, char[] answer) {
        this.newPass = Arrays.copyOf(newPass, newPass.length);
        this.oldPass = Arrays.copyOf(oldPass, oldPass.length);
        this.answer = Arrays.copyOf(answer, answer.length);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && new UserPrefs(context).isFingerprintEnabled()) {
            updateFingerprint(newPass);
        } else new AsyncReset().execute();
    }

    @Override
    public void onCancelled() {
        Toast.makeText(context, R.string.fingerprint_verify_cancelled, Toast.LENGTH_LONG).show();
        clearSensitiveData();
    }

    @Override
    public void onSuccess() {
        wasFingerprintUpdated = true;
        new AsyncReset().execute();
    }

    @Override
    public void onFailure() {
        Toast.makeText(context, R.string.fingerprint_failed, Toast.LENGTH_LONG).show();
        clearSensitiveData();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void updateFingerprint(char[] pass) {
        VerifyFingerprintDialog setupDialog = new VerifyFingerprintDialog();
        setupDialog.setTitle(context.getString(R.string.fingerprint_verify_title));
        setupDialog.setCancelable(false);
        setupDialog.setListener(this);
        setupDialog.setPassword(pass);
        setupDialog.show(fragmentManager, "Setup");
    }

    private void clearSensitiveData() {
        if (answer != null)
            Arrays.fill(answer, '\0');
        if (oldPass != null)
            Arrays.fill(oldPass, '\0');
        if (newPass != null)
            Arrays.fill(newPass, '\0');
    }

    public interface ResetPasswordListener {

        void onResetFinish(boolean success);

    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncReset extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            AuthData authData = new AuthData(context);
            try {
                Vault vault = Vault.get();
                if (vault.signIn(context, oldPass)) {
                    //re-encrypt the security password
                    authData.setEncryptedPassword(newPass, authData.getSecurityQuestion(), answer);
                    //update the password
                    vault.getManager().changePassword(newPass);
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to reset password", e);
            }
            //rollback fingerprint change
            if (wasFingerprintUpdated)
                authData.setEncryptedFingerprintPass(oldEncryptedPass);
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            clearSensitiveData();
            if (resetPasswordListener != null)
                resetPasswordListener.onResetFinish(success);
        }

    }

}
