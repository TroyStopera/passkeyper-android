package com.passkeyper.android.adapter;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.view.SecurityQuestionEditView;

/**
 * Class used to adapt/format SecurityQuesEntry objects.
 */
public class SecurityQuesAdapter extends PrivateVaultModelAdapter<SecurityQuesEntry> {

    private boolean verifyModeEnabled = false;

    public SecurityQuesAdapter(Context context) {
        super(context);
    }

    public void setVerifyModeEnabled(boolean enabled) {
        verifyModeEnabled = enabled;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SecurityQuestionEditView editView = new SecurityQuestionEditView(context, getItem(i));
        editView.setOnDeletePressedListener(this);

        if (i >= getCount() - 1) editView.setImeDone();

        TextInputLayout
                question = (TextInputLayout) editView.findViewById(R.id.edit_security_ques_input),
                answer = (TextInputLayout) editView.findViewById(R.id.edit_security_answer_input);

        //verify if enabled
        if (verifyModeEnabled) {
            TextInputEditText
                    questionText = (TextInputEditText) editView.findViewById(R.id.edit_security_ques_edit_text),
                    answerText = (TextInputEditText) editView.findViewById(R.id.edit_security_answer_edit_text);

            if (questionText.length() <= 0) {
                answer.setError(context.getString(R.string.error_missing_details));
                question.setError(context.getString(R.string.error_field_required));
            } else if (answerText.length() <= 0) {
                answer.setError(context.getString(R.string.error_missing_details));
            } else {
                question.setErrorEnabled(false);
                answer.setErrorEnabled(false);
            }
        } else {
            question.setErrorEnabled(false);
            answer.setErrorEnabled(false);
        }

        return editView;
    }

    @Override
    protected SecurityQuesEntry[] newArray(int size) {
        return new SecurityQuesEntry[size];
    }

}
