package com.passkeyper.android.vaultmodel;

/**
 * Represents a security question and an answer for an entry in the vault.
 */

public class SecurityQuesEntry extends VaultModel {

    private String question, answer;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

}
