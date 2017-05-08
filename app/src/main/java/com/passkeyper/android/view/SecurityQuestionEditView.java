package com.passkeyper.android.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;

/**
 * A custom View that encapsulates the functionality needed to edit a SecurityQuesEntry.
 */
@SuppressLint("ViewConstructor")
public class SecurityQuestionEditView extends PrivateVaultModelEditView<SecurityQuesEntry> implements View.OnFocusChangeListener {

    private static final TransformationMethod password = new PasswordTransformationMethod();

    private TextInputLayout questionInputLayout, answerInputLayout;
    private TextInputEditText questionEditText, answerEditText;

    public SecurityQuestionEditView(@NonNull Context context, SecurityQuesEntry entry) {
        super(context, R.layout.view_edit_security, entry);
    }

    public void setImeDone() {
        answerEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
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
    protected void onEditEnabled() {
        questionInputLayout.setVisibility(VISIBLE);
        answerInputLayout.setHint(getContext().getString(R.string.security_edit_answer));
        questionEditText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(questionEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    protected void onEditDisabled() {
        questionInputLayout.setVisibility(GONE);
        answerEditText.clearFocus();
        if (questionEditText.length() > 0 && answerEditText.length() > 0)
            answerInputLayout.setHint(questionEditText.getText());
        else answerInputLayout.setHint(getContext().getString(R.string.security_edit_new_hint));
    }

    @Override
    protected void onCensoredChange(boolean b) {
        if (b) answerEditText.setTransformationMethod(password);
        else answerEditText.setTransformationMethod(null);
    }

    @Override
    protected void onInitUiFields() {
        questionInputLayout = (TextInputLayout) findViewById(R.id.edit_security_ques_input);
        questionEditText = (TextInputEditText) findViewById(R.id.edit_security_ques_edit_text);
        answerInputLayout = (TextInputLayout) findViewById(R.id.edit_security_answer_input);
        answerEditText = (TextInputEditText) findViewById(R.id.edit_security_answer_edit_text);

        answerEditText.setOnFocusChangeListener(this);
        questionEditText.setOnFocusChangeListener(this);

        questionEditText.setNextFocusForwardId(answerEditText.getId());
    }

    @Override
    protected void onWriteToModel() {
        int len = answerEditText.length();
        final char[] chars = new char[len];
        //fill the array and return
        answerEditText.getText().getChars(0, len, chars, 0);
        model.setAnswer(chars);
        model.setQuestion(questionEditText.getText().toString());
    }

    @Override
    protected void updateUi() {
        if (model.hasQuestion()) {
            answerInputLayout.setHint(model.getQuestion());
            questionEditText.setText(model.getQuestion());
        } else answerInputLayout.setHint(getContext().getString(R.string.security_edit_new_hint));

        char[] answer = model.getAnswer();
        if (model.hasAnswer())
            answerEditText.setText(answer, 0, answer.length);
    }

}
