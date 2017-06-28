package com.passkeyper.android.fragment.setup;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.LocalSetupActivity;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.util.EditTextUtils;

import java.util.Arrays;

/**
 * LoginFragment for setting the password for the local vault.
 */
public class LocalSetup2Fragment extends AbstractLoginFragment<LocalSetupActivity> {

    private TextInputEditText password, confirm;

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_2_fragment, container, false);

        password = view.findViewById(R.id.input_password);
        confirm = view.findViewById(R.id.input_confirm);

        confirm.setOnEditorActionListener((v, actionId, event) -> {
            next();
            return true;
        });

        view.findViewById(R.id.back_btn).setOnClickListener(v -> loginFragmentActivity.pop());
        view.findViewById(R.id.next_btn).setOnClickListener(v -> next());

        return view;
    }

    char[] getPassword() {
        return EditTextUtils.getText(password);
    }

    private void next() {
        char[] passwordText = EditTextUtils.getText(password);
        char[] confirmText = EditTextUtils.getText(confirm);
        //password too short
        if (passwordText.length < 6) {
            password.setError(getString(R.string.error_password_short));
            window.startAnimation(shake);
        }
        //passwords don't match
        else if (!Arrays.equals(passwordText, confirmText)) {
            confirm.setError(getString(R.string.error_password_mismatch));
            window.startAnimation(shake);
        }
        //everything is fine
        else {
            password.setError(null);
            confirm.setError(null);
            loginFragmentActivity.replaceFragment(loginFragmentActivity.getSetup3Fragment(), true);
        }

        Arrays.fill(passwordText, '\0');
        Arrays.fill(confirmText, '\0');
    }

}
