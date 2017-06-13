package com.passkeyper.android.auth;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.passkeyper.android.R;

import java.util.Arrays;

import javax.crypto.Cipher;

/**
 * Dialog used to setup fingerprint authentication.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class VerifyFingerprintDialog extends DialogFragment implements FingerprintAuthHelper.OnAuthenticatedListener {

    private static final String TAG = "VerifyFingerprintDialog";

    private FingerprintAuthHelper fingerprintAuthHelper;
    private FingerprintSetupListener listener;
    private String title;
    private char[] password;
    private boolean wasAuthenticated = false;

    /**
     * Sets the title of the dialog.
     *
     * @param title the title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Set the object that should receive updates about the setup process.
     *
     * @param listener the FingerprintSetupListener that needs notified.
     */
    public void setListener(FingerprintSetupListener listener) {
        this.listener = listener;
    }

    /**
     * Set the password that needs to be encrypted during setup.
     * This method is required to be called before show().
     *
     * @param password the plain-text password.
     */
    public void setPassword(char[] password) {
        if (this.password != null)
            Arrays.fill(this.password, '\0');
        this.password = Arrays.copyOf(password, password.length);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.fingerprint_setup_dialog, null);

        fingerprintAuthHelper = new FingerprintAuthHelper(getContext(),
                (ImageView) view.findViewById(R.id.fingerprint_icon),
                (TextView) view.findViewById(R.id.touch_hint_tv),
                this);
        if (fingerprintAuthHelper.init(Cipher.ENCRYPT_MODE))
            fingerprintAuthHelper.startListening();
        else
            dismiss();

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setNegativeButton(android.R.string.cancel, (d, i) -> dismiss())
                .create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.password != null)
            Arrays.fill(this.password, '\0');
        if (fingerprintAuthHelper != null)
            fingerprintAuthHelper.stopListening();
        if (listener != null && !wasAuthenticated)
            listener.onCancelled();
    }

    @Override
    public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
        wasAuthenticated = true;
        if (password != null) {
            try {
                AuthData authData = new AuthData(getContext());
                authData.setEncryptedPassword(password, cryptoObject);
                if (listener != null)
                    listener.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Unable to setup fingerprint", e);
                if (listener != null)
                    listener.onFailure();
            } finally {
                dismiss();
            }
        }
        //no password means the setup cannot be finished
        else {
            Log.e(TAG, "Cannot show VerifyFingerprintDialog before setting the password");
            if (listener != null)
                listener.onFailure();
            dismiss();
        }
    }

    /**
     * Interface used to listen for events during a fingerprint authentication setup.
     */
    public interface FingerprintSetupListener {

        void onCancelled();

        void onSuccess();

        void onFailure();

    }

}
