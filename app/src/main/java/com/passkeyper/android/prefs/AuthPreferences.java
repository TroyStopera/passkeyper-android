package com.passkeyper.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Base64;

import com.passkeyper.android.util.ArrayConverter;

import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class used to access user settings related to authentication preferences.
 */
public class AuthPreferences extends Preferences {

    /* instance of AuthPreferences */
    private static AuthPreferences instance;
    /* variable names */
    private static final String PREF_APP_CLOSED_AUTH_TIMEOUT = "ClosedAuthTimeout";
    private static final String PREF_FINGERPRINT_ENABLED = "FingerPrintEnabled";
    private static final String PREF_SECURITY_QUESTION = "SecurityQuestion";
    private static final String PREF_ENCRYPTED_PASSWORD = "SecurityAnswerPasswd";

    private AuthPreferences(Context context) {
        super(context, "AuthPrefs");
    }

    /**
     * Returns the number of milliseconds after the app closes that the user is signed out.
     *
     * @return the time in milliseconds.
     */
    public long getAppClosedAuthTimeout() {
        return prefs().getLong(PREF_APP_CLOSED_AUTH_TIMEOUT, 30000);
    }

    /**
     * Sets the number of milliseconds after the app closes that the user is signed out.
     *
     * @param timeout the time in milliseconds.
     */
    public void setAppClosedAuthTimeout(long timeout) {
        SharedPreferences.Editor editor = edit();
        editor.putLong(PREF_APP_CLOSED_AUTH_TIMEOUT, timeout);
        editor.apply();
    }

    /**
     * Returns whether the fingerprint login is enabled.
     *
     * @return true if fingerprint authentication is enabled
     */
    public boolean isFingerprintEnabled() {
        return prefs().getBoolean(PREF_FINGERPRINT_ENABLED, false);
    }

    /**
     * Sets if fingerprint authentication is enabled.
     *
     * @param enabled whether fingerprint should be enabled;
     */
    public void setFingerprintEnabled(boolean enabled) {
        edit().putBoolean(PREF_FINGERPRINT_ENABLED, enabled).apply();
    }

    /**
     * Returns the user's security question.
     *
     * @return the security question.
     */
    @Nullable
    public String getSecurityQuestion() {
        return prefs().getString(PREF_SECURITY_QUESTION, null);
    }

    /**
     * Sets the user's sectiry question.
     *
     * @param securityQuestion the security question.
     */
    public void setSecurityQuestion(String securityQuestion) {
        edit().putString(PREF_SECURITY_QUESTION, securityQuestion).apply();
    }

    /**
     * Returns the encrypted password as a byte array.
     *
     * @param securityAnswer the answer to the user's security question.
     * @return the user's password.
     * @throws Exception when there is an issue with decryption.
     */
    @Nullable
    public char[] getDecryptedPassword(char[] securityAnswer) throws Exception {
        String password = prefs().getString(PREF_ENCRYPTED_PASSWORD, null);
        if (password == null) return null;

        Cipher cipher = getCipher(Cipher.DECRYPT_MODE, securityAnswer, getSecurityQuestion());
        return ArrayConverter.bytesToChars(cipher.doFinal(Base64.decode(password, Base64.DEFAULT)));
    }

    /**
     * Set the encrypted password that has been encrypted with the security answer.
     *
     * @param password         the vault password.
     * @param securityQuestion the security question.
     * @param securityAnswer   the security answer.
     * @throws Exception when there is an issue with encryption.
     */
    public void setEncryptedPassword(char[] password, String securityQuestion, char[] securityAnswer) throws Exception {
        Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, securityAnswer, securityQuestion);
        byte[] encryptedPass = cipher.doFinal(ArrayConverter.charsToBytes(password));
        edit().putString(PREF_ENCRYPTED_PASSWORD, Base64.encodeToString(encryptedPass, Base64.DEFAULT)).apply();
    }

    /**
     * Get the instance of AuthPreferences.
     *
     * @param context the Context used to load the SharedPreferences object.
     * @return the instance of AuthPreferences.
     */
    public static AuthPreferences get(Context context) {
        if (instance == null)
            instance = new AuthPreferences(context);
        return instance;
    }

    /*
        This method returns the roper cipher for use in getting and setting the encrypted password.
     */
    private static Cipher getCipher(int mode, char[] securityAnswer, String securityQuestion) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA1");
        KeySpec spec = new PBEKeySpec(
                securityAnswer,
                securityQuestion.getBytes(),
                65536,
                128
        );

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(mode, new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES"));
        return cipher;
    }

}
