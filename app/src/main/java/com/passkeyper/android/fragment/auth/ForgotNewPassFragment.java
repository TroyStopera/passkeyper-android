package com.passkeyper.android.fragment.auth;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.LocalSignInActivity;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.ResetPasswordHelper;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.util.EditTextUtils;

import java.util.Arrays;

/**
 * Fragment used to set a new password after the user has forgotten theirs.
 */
public class ForgotNewPassFragment extends AbstractLoginFragment<LocalSignInActivity>  implements ResetPasswordHelper.ResetPasswordListener {

    private TextInputEditText password, confirm;
    private ImageView icon;
    private ProgressBar loading;
    private ResetPasswordHelper resetPasswordHelper;

    @Override
    public void onResetFinish(boolean success) {
        loading.setVisibility(View.GONE);
        icon.setVisibility(View.VISIBLE);

        if (success) {
            loginFragmentActivity.redirectAndFinish();
        } else {
            loginFragmentActivity.pop();
            loginFragmentActivity.getForgotSecurityFragment().showWrongAnswer();
        }
    }

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.forgot_password_update_fragment, container, false);

        password = (TextInputEditText) view.findViewById(R.id.input_password);
        confirm = (TextInputEditText) view.findViewById(R.id.input_confirm);
        icon = (ImageView) view.findViewById(R.id.icon);
        loading = (ProgressBar) view.findViewById(R.id.loading);

        resetPasswordHelper = new ResetPasswordHelper(getContext(), getFragmentManager());
        resetPasswordHelper.setResetPasswordListener(this);

        view.findViewById(R.id.finish_btn).setOnClickListener((v) -> resetPassword());
        confirm.setOnEditorActionListener((v, actionId, event) -> {
            resetPassword();
            return true;
        });

        password.requestFocus();

        return view;
    }

    private void resetPassword() {
        //make sure the new password is valid
        char[] newPass = EditTextUtils.getText(password);
        char[] confirmNewPass = EditTextUtils.getText(confirm);
        //password too short
        if (newPass.length < 6) {
            password.setError(getString(R.string.error_password_short));
            window.startAnimation(shake);
        }
        //passwords don't match
        else if (!Arrays.equals(newPass, confirmNewPass)) {
            confirm.setError(getString(R.string.error_password_mismatch));
            window.startAnimation(shake);
        }
        //everything is fine
        else {
            password.setError(null);
            confirm.setError(null);

            char[] answer = null, oldPass = null;
            try {
                answer = loginFragmentActivity.getForgotSecurityFragment().getAnswer();
                oldPass = new AuthData(getContext()).getDecryptedPassword(answer);

                icon.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                resetPasswordHelper.reset(newPass, oldPass, answer);
            } catch (Exception e) {
                loginFragmentActivity.pop();
                loginFragmentActivity.getForgotSecurityFragment().showWrongAnswer();
            } finally {
                if (answer != null)
                    Arrays.fill(answer, '\0');
                if (oldPass != null)
                    Arrays.fill(oldPass, '\0');
            }
        }
        Arrays.fill(newPass, '\0');
        Arrays.fill(confirmNewPass, '\0');
    }

}
