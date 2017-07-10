package com.passkeyper.android.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.auth.AuthData;
import com.passkeyper.android.auth.PasswordResetHelper;
import com.passkeyper.android.util.EditTextUtils;
import com.passkeyper.android.vault.VaultManager;

import java.util.Arrays;

public class ManageLoginActivity extends AppCompatActivity {

    private static final TransformationMethod hidden = new PasswordTransformationMethod();
    private final Vault vault = Vault.get();

    private Button save;
    private TextInputEditText password, confirm, question, answer;
    private boolean passShown = false, confirmShown = false, quesShown = false, ansShown = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //security check
        if (!vault.hasManager() || vault.getManager().isClosed()) finish();

        setContentView(R.layout.activity_manage_login);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        password = findViewById(R.id.input_password);
        confirm = findViewById(R.id.input_confirm);
        question = findViewById(R.id.input_question);
        answer = findViewById(R.id.input_answer);

        save = findViewById(R.id.btn_save);
        save.setOnClickListener((v) -> save());

        findViewById(R.id.pass_visible).setOnClickListener(
                (v) -> passShown = toggleHide(password, (ImageView) v, passShown));
        findViewById(R.id.confirm_visible).setOnClickListener(
                (v) -> confirmShown = toggleHide(confirm, (ImageView) v, confirmShown));
        findViewById(R.id.question_visible).setOnClickListener(
                (v) -> quesShown = toggleHide(question, (ImageView) v, quesShown));
        findViewById(R.id.answer_visible).setOnClickListener(
                (v) -> ansShown = toggleHide(answer, (ImageView) v, ansShown));
    }

    @Override
    protected void onPause() {
        super.onPause();
        password.setText(null);
        confirm.setText(null);
        question.setText(null);
        answer.setText(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VaultManager.RecoveryData recoveryData = vault.getManager().getRecoveryData();

        question.setText(recoveryData.getSecurityQuestion());
        char[] answerText = recoveryData.getSecurityAnswer();
        answer.setText(answerText, 0, answerText.length);
        Arrays.fill(answerText, '\0');

        recoveryData.free();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    private void save() {
        //ensure input meets requirements
        if (validateInput())
            new SaveHelper(
                    vault.getManager().getPassword(),
                    EditTextUtils.getText(password),
                    question.getText().toString(),
                    EditTextUtils.getText(answer)
            ).save();
    }

    private boolean validateInput() {
        boolean valid = true;

        password.setError(null);
        confirm.setError(null);
        question.setError(null);
        answer.setError(null);

        if (question.getText().length() <= 0) {
            question.setError(getString(R.string.error_field_required));
            valid = false;
        }

        if (answer.getText().length() <= 0) {
            answer.setError(getString(R.string.error_field_required));
            valid = false;
        }

        String passwordText = password.getText().toString();
        String confirmText = confirm.getText().toString();

        if (!passwordText.isEmpty() && confirmText.isEmpty()) {
            confirm.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (passwordText.isEmpty() && !confirmText.isEmpty()) {
            password.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!passwordText.isEmpty() && !confirmText.isEmpty()) {
            if (passwordText.length() < 6) {
                password.setError(getString(R.string.error_password_short));
                valid = false;
            } else if (!passwordText.equals(confirmText)) {
                confirm.setError(getString(R.string.error_password_mismatch));
                valid = false;
            }
        }

        return valid;
    }

    private boolean toggleHide(EditText editText, ImageView icon, boolean hide) {
        if (hide) {
            editText.setTransformationMethod(hidden);
            icon.setImageResource(R.drawable.ic_visibility_off);
        } else {
            editText.setTransformationMethod(null);
            icon.setImageResource(R.drawable.ic_visibility_on);
        }
        return !hide;
    }

    /*
        This class is a helper class for updating sign in info.
        It has its own reference to all needed data, so if the user
        backs out of the activity before the app is done saving, the save will still succeed.
     */
    private class SaveHelper {

        private final char[] oldPass, password, answer;
        private final String question;

        private SaveHelper(char[] oldPass, char[] password, String question, char[] answer) {
            this.oldPass = oldPass;
            this.password = password;
            this.question = question;
            this.answer = answer;
        }

        private void save() {
            //update button, it will be reset after security is updated
            save.setText(R.string.manage_login_saving);
            save.setClickable(false);
            //if changing password
            if (password.length >= 6) {
                PasswordResetHelper resetHelper = new PasswordResetHelper(ManageLoginActivity.this, getSupportFragmentManager());
                resetHelper.setResetPasswordListener((success) -> {
                    if (success) saveSecurityQues();
                    else {
                        save.setText(R.string.action_save);
                        save.setClickable(true);
                    }
                });
                resetHelper.reset(password, oldPass, answer);
            }
            //if only changing security question
            else {
                saveSecurityQues();
            }
        }

        private void saveSecurityQues() {
            try {
                AuthData authData = new AuthData(ManageLoginActivity.this);
                authData.setEncryptedPassword(password, question, answer);
                authData.setSecurityQuestion(question);

                VaultManager.RecoveryData recoveryData = Vault.get().getManager().getRecoveryData();
                recoveryData.setSecurityQuestion(question);
                recoveryData.setSecurityAnswer(answer);
                Vault.get().getManager().updateRecoveryData(recoveryData);
                recoveryData.free();

                save.setText(R.string.manage_login_saved);
            } catch (Exception e) {
                Log.e("Manage Sign in", "Unable to update security question/answer", e);
                save.setText(R.string.manage_login_error);
                Toast.makeText(ManageLoginActivity.this, R.string.manage_login_error, Toast.LENGTH_SHORT).show();
            } finally {
                Arrays.fill(oldPass, '\0');
                Arrays.fill(password, '\0');
                Arrays.fill(answer, '\0');
                new Handler(getMainLooper()).postDelayed(() -> {
                    save.setText(R.string.action_save);
                    save.setClickable(true);
                }, 3000);
            }
        }

    }

}
