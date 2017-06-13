package com.passkeyper.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.passkeyper.android.fragment.setup.LocalSetup1Fragment;
import com.passkeyper.android.fragment.setup.LocalSetup2Fragment;
import com.passkeyper.android.fragment.setup.LocalSetup3Fragment;
import com.passkeyper.android.fragment.setup.LocalSetup4Fragment;

public class InitialSetupActivity extends AbstractLoginActivity {

    private final LocalSetup1Fragment setup1Fragment = new LocalSetup1Fragment();
    private final LocalSetup2Fragment setup2Fragment = new LocalSetup2Fragment();
    private final LocalSetup3Fragment setup3Fragment = new LocalSetup3Fragment();
    private final LocalSetup4Fragment setup4Fragment = new LocalSetup4Fragment();

    public LocalSetup1Fragment getSetup1Fragment() {
        return setup1Fragment;
    }

    public LocalSetup2Fragment getSetup2Fragment() {
        return setup2Fragment;
    }

    public LocalSetup3Fragment getSetup3Fragment() {
        return setup3Fragment;
    }

    public LocalSetup4Fragment getSetup4Fragment() {
        return setup4Fragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitialFragment(setup1Fragment);
    }

}
