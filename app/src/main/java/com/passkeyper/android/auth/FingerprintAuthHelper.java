package com.passkeyper.android.auth;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.passkeyper.android.R;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

/**
 * Helper class that is used to initiate and animate a fingerprint touch UI.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintAuthHelper extends FingerprintManager.AuthenticationCallback {

    private static final String TAG = "Fingerprint Auth";
    private static final String KEY_NAME = "FingerPrint_Auth_Key";

    private final Context context;
    private final ImageView icon;
    private final TextView hint;
    private final OnAuthenticatedListener authenticatedListener;
    private final FingerprintManager fingerprintManager;
    /* runnable used to reset the touch tag error */
    private final Runnable reset = new Runnable() {
        @Override
        public void run() {
            hint.setTextColor(context.getResources().getColor(R.color.touch_hint_color));
            hint.setText(context.getResources().getText(R.string.login_touch_sensor));
            icon.setImageResource(R.drawable.ic_fingerprint);
            icon.setBackgroundResource(R.drawable.fingerprint_bg);
        }
    };

    private FingerprintManager.CryptoObject cryptoObject;
    private boolean isInitialized = false, isListening = false;
    private CancellationSignal cancellationSignal;

    public FingerprintAuthHelper(Context context, ImageView icon, TextView hint, OnAuthenticatedListener authenticatedListener) {
        this.context = context;
        this.icon = icon;
        this.hint = hint;
        this.authenticatedListener = authenticatedListener;
        fingerprintManager = context.getSystemService(FingerprintManager.class);
    }

    public boolean init(int cipherMode) {
        if (!isAvailable(context)) {
            isInitialized = false;
            return false;
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            //ensure the key has been generated
            if (!keyStore.containsAlias(KEY_NAME) && !createKey()) {
                isInitialized = false;
                return false;
            }
            //setup the CryptoObject
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);

            if (cipherMode == Cipher.ENCRYPT_MODE) {
                cipher.init(Cipher.ENCRYPT_MODE, keyStore.getKey(KEY_NAME, null));
                new AuthData(context).setFingerprintIv(cipher.getIV());
            } else if (cipherMode == Cipher.DECRYPT_MODE) {
                IvParameterSpec ivParams = new IvParameterSpec(new AuthData(context).getFingerprintIv());
                cipher.init(Cipher.DECRYPT_MODE, keyStore.getKey(KEY_NAME, null), ivParams);
            }

            cryptoObject = new FingerprintManager.CryptoObject(cipher);

            isInitialized = true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing FingerprintAuthHelper", e);
            isInitialized = false;
        }
        return isInitialized;
    }

    public void startListening() {
        if (isInitialized) {
            isListening = true;
            cancellationSignal = new CancellationSignal();
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }
    }

    public void stopListening() {
        if (cancellationSignal != null) {
            isListening = false;
            cancellationSignal.cancel();
            cancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (isListening)
            setError(errString);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        if (isListening)
            setError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        if (isListening)
            setError(context.getString(R.string.fingerprint_not_recognized));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        hint.setText(context.getString(R.string.fingerprint_success));
        hint.setTextColor(context.getResources().getColor(R.color.touch_success_color));
        icon.setImageResource(R.drawable.ic_check);
        icon.removeCallbacks(reset);
        icon.postDelayed(reset, 1600);

        icon.post(() -> authenticatedListener.onAuthenticated(cryptoObject));
    }

    private void setError(CharSequence text) {
        hint.setText(text);
        hint.setTextColor(context.getResources().getColor(R.color.touch_warning_color));
        icon.setImageResource(R.drawable.ic_error);
        icon.setBackgroundResource(R.drawable.fingerprint_error_bg);

        icon.removeCallbacks(reset);
        icon.postDelayed(reset, 1600);
    }

    private boolean createKey() {
        try {
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                    KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // TODO: invalidate fingerprint key
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(false);
            }

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Unable to create fingerprint key", e);
            return false;
        }
    }

    public static boolean isAvailable(Context context) {
        FingerprintManager fingerprintManager = context.getSystemService(FingerprintManager.class);
        return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
    }

    /**
     * Listener class for when a user authenticates with a fingerprint.
     */
    public interface OnAuthenticatedListener {

        void onAuthenticated(FingerprintManager.CryptoObject cryptoObject);

    }

}
