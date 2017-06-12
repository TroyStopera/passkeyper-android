package com.passkeyper.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.passkeyper.android.fragment.FingerprintSignInFragment;
import com.passkeyper.android.fragment.LocalSignInFragment;
import com.passkeyper.android.prefs.UserPreferences;

public class LocalLoginActivity extends AbstractLoginActivity {

    private final LocalSignInFragment localSignInFragment = new LocalSignInFragment();
    private final FingerprintSignInFragment fingerprintSignInFragment = new FingerprintSignInFragment();

    public LocalSignInFragment getLocalSignInFragment() {
        return localSignInFragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserPreferences.get(this).isFingerprintEnabled())
            setInitialFragment(fingerprintSignInFragment);
        else
            setInitialFragment(localSignInFragment);
    }

}
