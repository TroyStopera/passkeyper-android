package com.passkeyper.android.fragment.setup;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.InitialSetupActivity;
import com.passkeyper.android.fragment.AbstractLoginFragment;

/**
 * LoginFragment for the starting screen for setting up the local vault.
 */
public class LocalSetup1Fragment extends AbstractLoginFragment<InitialSetupActivity> {

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_1_fragment, container, false);
        view.findViewById(R.id.setup_begin_btn).setOnClickListener(v ->
                loginFragmentActivity.replaceFragment(loginFragmentActivity.getSetup2Fragment(), true)
        );
        return view;
    }

}
