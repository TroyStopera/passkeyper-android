package com.passkeyper.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;

/**
 * A custom View that encapsulates the functionality needed to edit a SecurityQuesEntry.
 */
public class SecurityQuestionEditView extends FrameLayout implements View.OnFocusChangeListener, View.OnClickListener {

    private static final TransformationMethod password = new PasswordTransformationMethod();

    private TextInputLayout questionInputLayout, answerInputLayout;
    private TextInputEditText questionEditText, answerEditText;
    private ImageButton visibilityButton, deleteButton, doneButton;

    private boolean isTextCensored = true;
    private OnDeletePressedListener listener;

    public SecurityQuestionEditView(@NonNull Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_edit_security, this);

        setUiFields();
    }

    public SecurityQuestionEditView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_edit_security, this);

        setUiFields();
    }

    public SecurityQuesEntry getSecurityQuesEntry(EntryRecord record) {
        SecurityQuesEntry entry = new SecurityQuesEntry(record);
        entry.setQuestion(getQuestion());
        entry.setAnswer(getAnswer());
        return entry;
    }

    public void setSecurityQuesEntry(SecurityQuesEntry securityQuesEntry) {
        String question = securityQuesEntry.getQuestion();
        char[] answer = securityQuesEntry.getAnswer();

        questionEditText.setText(question);
        answerEditText.setText(answer, 0, answer.length);
        if (question == null || question.isEmpty())
            answerInputLayout.setHint(getContext().getString(R.string.security_edit_new_hint));
        else answerInputLayout.setHint(question);
    }

    public String getQuestion() {
        return questionEditText.getText().toString();
    }

    public char[] getAnswer() {
        int len = answerEditText.length();
        final char[] chars = new char[len];
        //fill the array and return
        answerEditText.getText().getChars(0, len, chars, 0);
        return chars;
    }

    public void setDeletePressedListener(OnDeletePressedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.edit_security_answer_edit_text && hasFocus)
            setEditMode(true);
        else if (view.getId() == R.id.edit_security_ques_edit_text && hasFocus)
            setEditMode(true);
        else if (!questionEditText.hasFocus() && !answerEditText.hasFocus())
            setEditMode(false);
    }

    @Override
    public void onClick(View view) {
        // send a delete pressed event
        if (view.getId() == R.id.edit_security_delete) {
            if (listener != null) listener.onDeletePressed(this);
        }
        // hide edit mode
        else if (view.getId() == R.id.edit_security_done) {
            setEditMode(false);
        }
        // toggle text visibility
        else if (view.getId() == R.id.edit_security_visible) {
            if (isTextCensored) {
                visibilityButton.setImageResource(R.drawable.ic_visibility_on);
                answerEditText.setTransformationMethod(null);
            } else {
                visibilityButton.setImageResource(R.drawable.ic_visibility_off);
                answerEditText.setTransformationMethod(password);
            }
            isTextCensored = !isTextCensored;
        }
    }

    private void setUiFields() {
        questionInputLayout = (TextInputLayout) findViewById(R.id.edit_security_ques_input);
        questionEditText = (TextInputEditText) findViewById(R.id.edit_security_ques_edit_text);
        answerInputLayout = (TextInputLayout) findViewById(R.id.edit_security_answer_input);
        answerEditText = (TextInputEditText) findViewById(R.id.edit_security_answer_edit_text);
        visibilityButton = (ImageButton) findViewById(R.id.edit_security_visible);
        deleteButton = (ImageButton) findViewById(R.id.edit_security_delete);
        doneButton = (ImageButton) findViewById(R.id.edit_security_done);

        answerEditText.setOnFocusChangeListener(this);
        questionEditText.setOnFocusChangeListener(this);
        visibilityButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
    }

    private void setEditMode(boolean enabled) {
        if (enabled) {
            questionInputLayout.setVisibility(VISIBLE);
            doneButton.setVisibility(VISIBLE);
            deleteButton.setVisibility(GONE);
            answerInputLayout.setHint(getContext().getString(R.string.security_edit_answer));
        } else {
            questionInputLayout.setVisibility(GONE);
            doneButton.setVisibility(GONE);
            deleteButton.setVisibility(VISIBLE);
            answerEditText.clearFocus();
            if (questionEditText.length() > 0 && answerEditText.length() > 0)
                answerInputLayout.setHint(questionEditText.getText());
            else answerInputLayout.setHint(getContext().getString(R.string.security_edit_new_hint));
        }
    }

    public interface OnDeletePressedListener {

        void onDeletePressed(SecurityQuestionEditView view);

    }

}
