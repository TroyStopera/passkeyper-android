package com.passkeyper.android.fragment;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.LocalLoginActivity;

import java.util.Arrays;

/**
 * LoginFragment for signing into the local vault.
 */
public class LocalSignInFragment extends AbstractLoginFragment<LocalLoginActivity> {

    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordInput;

    @Override
    View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_login_fragment, container, false);

        passwordInputLayout = (TextInputLayout) view.findViewById(R.id.input_layout_password);
        passwordInput = (TextInputEditText) view.findViewById(R.id.input_password);
        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            signIn();
            return true;
        });

        view.findViewById(R.id.sign_in_btn).setOnClickListener(v -> signIn());
        view.findViewById(R.id.sign_in_forgot).setOnClickListener(v -> forgotPassword());

        return view;
    }

    private void signIn() {
        passwordInputLayout.setErrorEnabled(false);

        //get the password and clear the input for security
        int len = passwordInput.length();
        char[] password = new char[len];
        passwordInput.getText().getChars(0, len, password, 0);
        passwordInput.getText().clear();

        if (vault.signInToLocalVault(getContext(), password)) {
            Arrays.fill(password, '\0');
            loginFragmentActivity.redirectAndFinish();
        } else {
            Arrays.fill(password, '\0');
            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(getString(R.string.error_incorrect_password));

            Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
            window.startAnimation(shake);
        }
    }

    private void forgotPassword() {
        //TODO: implement forgot password
    }

}
