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
import com.passkeyper.android.fragment.LocalSignInFragment;

public class LoginActivity extends FragmentActivity implements AbstractLoginFragment.LoginFragmentActivity {

    private static final String TAG = "Login";

    private final Vault vault = Vault.get();

    private String nextActivityName;

    @Override
    public void replace(AbstractLoginFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.replace(R.id.login_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void redirectAndFinish() {
        //launch the next activity if there is one
        if (nextActivityName != null) try {
            startActivity(new Intent(this, Class.forName(nextActivityName)));
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Unable redirect to Activity '" + nextActivityName + "'", e);
        }

        finish();
    }

    @Override
    public void onBackPressed() {        
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
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
        transaction.add(R.id.login_fragment_container, new LocalSignInFragment());
        transaction.commit();
    }

}
