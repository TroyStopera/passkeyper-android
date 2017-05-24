package com.passkeyper.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.fragment.AbstractLoginFragment;

/**
 * Abstract Activity that is the basis for logging in and setting up the app.
 */
public abstract class AbstractLoginActivity extends FragmentActivity {

    private static final String TAG = "Login";

    private final Vault vault = Vault.get();

    private String nextActivityName;

    protected abstract AbstractLoginFragment getFirstFragment();

    public final boolean pop() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return false;
    }

    public final void replaceFragment(AbstractLoginFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.replace(R.id.login_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public final void redirectAndFinish() {
        //launch the next activity if there is one
        if (nextActivityName != null) try {
            startActivity(new Intent(this, Class.forName(nextActivityName)));
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Unable redirect to Activity '" + nextActivityName + "'", e);
        }

        finish();
    }

    @Override
    public final void onBackPressed() {
        if (!pop()) {
            //ensure the vault manager is closed if there are no more fragments
            vault.signOut();
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().hasExtra(Vault.ACTIVITY_AFTER_SIGN_IN_EXTRA))
            nextActivityName = getIntent().getStringExtra(Vault.ACTIVITY_AFTER_SIGN_IN_EXTRA);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(0, 0, R.anim.in_from_left, R.anim.out_to_right);
        transaction.add(R.id.login_fragment_container, getFirstFragment());
        transaction.commit();
    }

}
