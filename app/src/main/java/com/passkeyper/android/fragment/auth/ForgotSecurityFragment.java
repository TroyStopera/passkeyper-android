package com.passkeyper.android.fragment.auth;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.LocalSignInActivity;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.util.EditTextUtils;

import java.util.Arrays;

/**
 * Fragment used to very a user's security question/answer when they have forgotten their password.
 */
public class ForgotSecurityFragment extends AbstractLoginFragment<LocalSignInActivity> {

    private TextInputEditText answer;
    private ImageView icon;
    private ProgressBar loading;

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.forgot_password_security_fragment, container, false);

        answer = view.findViewById(R.id.input_answer);
        icon = view.findViewById(R.id.icon);
        loading = view.findViewById(R.id.loading);

        AuthData authData = new AuthData(getContext());
        ((TextView) view.findViewById(R.id.security_question)).setText(authData.getSecurityQuestion());

        view.findViewById(R.id.next_btn).setOnClickListener((v) -> new AsyncVerifyAnswer().execute());
        answer.setOnEditorActionListener((v, actionId, event) -> {
            new AsyncVerifyAnswer().execute();
            return true;
        });

        return view;
    }

    char[] getAnswer() {
        return EditTextUtils.getText(answer);
    }

    void showWrongAnswer() {
        answer.setError(getString(R.string.forgot_password_wrong_answer));
        window.startAnimation(shake);
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncVerifyAnswer extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            icon.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            char[] answerText = null, pass = null;
            try {
                AuthData authData = new AuthData(getContext());
                answerText = EditTextUtils.getText(answer);
                pass = authData.getDecryptedPassword(answerText);

                if (vault.signIn(getContext(), pass)) {
                    vault.signOut();
                    return true;
                } else return false;
            } catch (Exception e) {
                return false;
            } finally {
                if (answerText != null)
                    Arrays.fill(answerText, '\0');
                if (pass != null)
                    Arrays.fill(pass, '\0');
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            loading.setVisibility(View.GONE);
            icon.setVisibility(View.VISIBLE);

            if (success)
                loginFragmentActivity.replaceFragment(loginFragmentActivity.getForgotNewPassFragment(), true);
            else showWrongAnswer();
        }

    }

}
