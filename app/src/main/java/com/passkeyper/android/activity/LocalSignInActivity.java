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

import com.passkeyper.android.Vault;
import com.passkeyper.android.R;

import java.util.Arrays;

public class LocalSignInActivity extends AppCompatActivity {

    private static final String TAG = "Local Sign In";

    private Vault vault;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordInput;
    private String nextActivityName;

    public void signIn(View view) {
        passwordInputLayout.setErrorEnabled(false);

        //get the password and clear the input for security
        int len = passwordInput.length();
        char[] password = new char[len];
        passwordInput.getText().getChars(0, len, password, 0);
        passwordInput.getText().clear();

        if (vault.signInToLocalVault(this, password)) {
            Arrays.fill(password, '\0');
            //launch the next activity if there is one
            if (nextActivityName != null) try {
                Class<?> clazz = Class.forName(nextActivityName);
                Intent intent = new Intent(this, clazz);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Log.w(TAG, "Unable redirect to Activity '" + nextActivityName + "'", e);
            }
            finish();
        } else {
            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(getString(R.string.error_incorrect_password));
            Arrays.fill(password, '\0');
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //ensure the vault manager is closed
        vault.signOut();
        finishAffinity();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_login);

        Intent intent = getIntent();
        if (intent.hasExtra(Vault.ACTIVITY_AFTER_SIGN_IN_EXTRA))
            nextActivityName = intent.getStringExtra(Vault.ACTIVITY_AFTER_SIGN_IN_EXTRA);

        vault = Vault.get();
        passwordInputLayout = (TextInputLayout) findViewById(R.id.input_layout_password);
        passwordInput = (TextInputEditText) findViewById(R.id.input_password);

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                signIn(findViewById(R.id.sign_in_btn));
                return true;
            }
            return false;
        });
    }

}
