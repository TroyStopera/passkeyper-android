package com.passkeyper.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.passkeyper.android.fragment.auth.FingerprintSignInFragment;
import com.passkeyper.android.fragment.auth.ForgotNewPassFragment;
import com.passkeyper.android.fragment.auth.ForgotSecurityFragment;
import com.passkeyper.android.fragment.auth.LocalSignInFragment;
import com.passkeyper.android.UserPreferences;

public class LocalSignInActivity extends AbstractLoginActivity {

    private final LocalSignInFragment localSignInFragment = new LocalSignInFragment();
    private final ForgotNewPassFragment forgotNewPassFragment = new ForgotNewPassFragment();
    private final ForgotSecurityFragment forgotSecurityFragment = new ForgotSecurityFragment();

    public LocalSignInFragment getLocalSignInFragment() {
        return localSignInFragment;
    }

    public ForgotSecurityFragment getForgotSecurityFragment() {
        return forgotSecurityFragment;
    }

    public ForgotNewPassFragment getForgotNewPassFragment() {
        return forgotNewPassFragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (new UserPreferences(this).isFingerprintEnabled())
            //don't save FingerprintSignIn as a field because it requires API 23+
            setInitialFragment(new FingerprintSignInFragment());
        else
            setInitialFragment(localSignInFragment);
    }

}
