package com.passkeyper.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.passkeyper.android.AppVault;
import com.passkeyper.android.R;

import java.util.Arrays;

public class LocalLoginActivity extends AppCompatActivity {

    private static final String TAG = "Local Sign In";

    private AppVault mAppVault;
    private TextInputLayout mPasswordInputLayout;
    private TextInputEditText mPasswordInput;
    private String mNextActivityName;

    public void signIn(View view) {
        mPasswordInputLayout.setErrorEnabled(false);

        int len = mPasswordInput.length();
        char[] password = new char[len];
        mPasswordInput.getText().getChars(0, len, password, 0);
        mPasswordInput.getText().clear();

        if (mAppVault.signInToLocalVault(this, password)) {
            Arrays.fill(password, '\0');
            //launch the next activity if there is one
            if (mNextActivityName != null) try {
                Class<?> clazz = Class.forName(mNextActivityName);
                Intent intent = new Intent(this, clazz);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Log.w(TAG, "Unable redirect to Activity '" + mNextActivityName + "'", e);
            }
            finish();
        } else {
            mPasswordInputLayout.setErrorEnabled(true);
            mPasswordInputLayout.setError(getString(R.string.error_incorrect_password));
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //ensure the vault manager is closed
        mAppVault.signOut();
        finishAffinity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_login);

        Intent intent = getIntent();
        if (intent.hasExtra(AppVault.ACTIVITY_AFTER_SIGN_IN_EXTRA))
            mNextActivityName = intent.getStringExtra(AppVault.ACTIVITY_AFTER_SIGN_IN_EXTRA);

        mAppVault = AppVault.get();
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_password);
        mPasswordInput = (TextInputEditText) findViewById(R.id.input_password);

        mPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    signIn(findViewById(R.id.sign_in_btn));
                    return true;
                }
                return false;
            }
        });
    }

}
