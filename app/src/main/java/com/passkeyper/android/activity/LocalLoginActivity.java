package com.passkeyper.android.activity;

import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.fragment.LocalSignInFragment;

public class LocalLoginActivity extends AbstractLoginActivity {

    @Override
    protected AbstractLoginFragment getFirstFragment() {
        return new LocalSignInFragment();
    }

}
