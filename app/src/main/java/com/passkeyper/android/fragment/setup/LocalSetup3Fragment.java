package com.passkeyper.android.fragment.setup;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.R;
import com.passkeyper.android.activity.InitialSetupActivity;
import com.passkeyper.android.fragment.AbstractLoginFragment;
import com.passkeyper.android.util.EditTextUtils;

/**
 * LoginFragment for setting the security question and answer for the local vault.
 */
public class LocalSetup3Fragment extends AbstractLoginFragment<InitialSetupActivity> {

    private TextInputEditText question, answer;

    @Override
    protected View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.local_setup_3_fragment, container, false);

        question = (TextInputEditText) view.findViewById(R.id.input_question);
        answer = (TextInputEditText) view.findViewById(R.id.input_answer);

        answer.setOnEditorActionListener((v, actionId, event) -> {
            next();
            return true;
        });

        view.findViewById(R.id.back_btn).setOnClickListener(v -> loginFragmentActivity.pop());
        view.findViewById(R.id.next_btn).setOnClickListener(v -> next());

        question.requestFocus();

        return view;
    }

    String getQuestion() {
        return question.getText().toString();
    }

    char[] getAnswer() {
        return EditTextUtils.getText(answer);
    }

    private void next() {
        char[] questionText = EditTextUtils.getText(question);
        char[] answerText = EditTextUtils.getText(answer);
        boolean valid = true;

        if (questionText.length <= 0) {
            question.setError(getString(R.string.error_field_required));
            valid = false;
        }

        if (answerText.length <= 0) {
            answer.setError(getString(R.string.error_field_required));
            valid = false;
        }

        if (valid) {
            loginFragmentActivity.replaceFragment(loginFragmentActivity.getSetup4Fragment(), true);
        } else window.startAnimation(shake);
    }

}
