package com.passkeyper.android.fragment.auth;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.LocalLoginActivity;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.FingerprintAuthHelper;
import com.passkeyper.android.fragment.AbstractLoginFragment;

import javax.crypto.Cipher;

/**
 * LoginFragment for signing in with a fingerprint.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintSignInFragment extends AbstractLoginFragment<LocalLoginActivity> implements FingerprintAuthHelper.OnAuthenticatedListener {

    private static final String TAG = "Fingerprint Sign in";

    private FingerprintAuthHelper fingerprintAuthHelper;

    @Override
    public void onAuthenticated(FingerprintManager.CryptoObject cryptoObject) {
        try {
            AuthData authData = new AuthData(getContext());
            if (vault.signInToLocalVault(getContext(), authData.getDecryptedPassword(cryptoObject)))
                loginFragmentActivity.redirectAndFinish();
        } catch (Exception e) {
            Log.e(TAG, "Unable to decrypt fingerprint password", e);
            loginFragmentActivity.replaceFragment(loginFragmentActivity.getLocalSignInFragment(), false);
            Toast.makeText(getContext(), getContext().getString(R.string.fingerprint_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fingerprint_login_fragment, container, false);
        view.findViewById(R.id.use_password_btn).setOnClickListener(v -> cancelFingerprint());

        fingerprintAuthHelper = new FingerprintAuthHelper(getContext(),
                (ImageView) view.findViewById(R.id.fingerprint_icon),
                (TextView) view.findViewById(R.id.touch_hint_tv),
                this);

        if (!fingerprintAuthHelper.init(Cipher.DECRYPT_MODE)) {
            loginFragmentActivity.replaceFragment(loginFragmentActivity.getLocalSignInFragment(), false);
            Toast.makeText(getContext(), getContext().getString(R.string.fingerprint_error), Toast.LENGTH_LONG).show();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fingerprintAuthHelper.startListening();
    }

    @Override
    public void onPause() {
        super.onPause();
        fingerprintAuthHelper.stopListening();
    }

    private void cancelFingerprint() {
        fingerprintAuthHelper.stopListening();
        loginFragmentActivity.replaceFragment(loginFragmentActivity.getLocalSignInFragment(), true);
    }

}
