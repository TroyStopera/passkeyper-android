package com.passkeyper.android.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import com.passkeyper.android.util.ArrayConverter;

import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class used to access data needed during the login process.
 */
public class AuthData {

    /* variable names */
    private static final String PREF_SECURITY_QUESTION = "SecurityQuestion";
    private static final String PREF_ENCRYPTED_PASSWORD_SQ = "SecurityAnswerPasswd";
    private static final String PREF_ENCRYPTED_PASSWORD_FP = "FingerprintPasswd";
    private static final String PREF_FINGERPRINT_IV = "FingerprintIV";
    private static final String PREF_SECURITY_IV = "SecurityIV";

    private final SharedPreferences prefs;

    public AuthData(Context context) {
        prefs = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE);
    }

    /**
     * Removes the fingerprint encrypted password. Used when fingerprint sign in is disabled.
     */
    public void clearFingerprintPassword() {
        prefs.edit().remove(PREF_ENCRYPTED_PASSWORD_FP).apply();
    }

    /**
     * Returns the fingerprint decrypted password as a char array.
     *
     * @param cryptoObject the fingerprint CryptoObject used to decrypt.
     * @return the password.
     * @throws Exception when there is an issue with decryption.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public char[] getDecryptedPassword(FingerprintManager.CryptoObject cryptoObject) throws Exception {
        String password = prefs.getString(PREF_ENCRYPTED_PASSWORD_FP, null);
        if (password == null) return null;

        return ArrayConverter.bytesToChars(cryptoObject.getCipher().doFinal(Base64.decode(password, Base64.DEFAULT)));
    }

    /**
     * Set the encrypted password that has been encrypted with the fingerprint.
     *
     * @param password     the plain-text password.
     * @param cryptoObject the fingerprint CryptoObject used to encrypt.
     * @throws Exception when there is an issue with encryption.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setEncryptedPassword(char[] password, FingerprintManager.CryptoObject cryptoObject) throws Exception {
        byte[] encryptedPass = cryptoObject.getCipher().doFinal(ArrayConverter.charsToBytes(password));
        prefs.edit().putString(PREF_ENCRYPTED_PASSWORD_FP, Base64.encodeToString(encryptedPass, Base64.DEFAULT)).apply();
    }

    /**
     * Returns the raw password encrypted by the fingerprint CryptoObject as an encrypted string.
     *
     * @return teh encrypted password.
     */
    public String getEncryptedFingerprintPass() {
        return prefs.getString(PREF_ENCRYPTED_PASSWORD_FP, null);
    }

    /**
     * Sets the password encrypted by the fingerprint CryptoObject. used when undoing a change.
     *
     * @param pass the password.
     */
    public void setEncryptedFingerprintPass(String pass) {
        prefs.edit().putString(PREF_ENCRYPTED_PASSWORD_FP, pass).apply();
    }

    /**
     * Returns the initialization vector used while encrypting the fingerprint key.
     *
     * @return the initialization vector.
     */
    public byte[] getFingerprintIv() {
        String ivString = prefs.getString(PREF_FINGERPRINT_IV, null);
        if (ivString == null) return null;

        return Base64.decode(ivString, Base64.DEFAULT);
    }

    /**
     * Sets the initialization vector used while encrypting the fingerprint key.
     *
     * @param iv the initialization vector.
     */
    public void setFingerprintIv(byte[] iv) {
        prefs.edit().putString(PREF_FINGERPRINT_IV, Base64.encodeToString(iv, Base64.DEFAULT)).apply();
    }

    /**
     * Returns the user's security question.
     *
     * @return the security question.
     */
    @Nullable
    public String getSecurityQuestion() {
        return prefs.getString(PREF_SECURITY_QUESTION, null);
    }

    /**
     * Sets the user's security question.
     *
     * @param securityQuestion the security question.
     */
    public void setSecurityQuestion(String securityQuestion) {
        prefs.edit().putString(PREF_SECURITY_QUESTION, securityQuestion).apply();
    }

    /**
     * Returns the security question decrypted password as a char array.
     *
     * @param securityAnswer the answer to the user's security question.
     * @return the user's password.
     * @throws Exception when there is an issue with decryption.
     */
    @Nullable
    public char[] getDecryptedPassword(char[] securityAnswer) throws Exception {
        String password = prefs.getString(PREF_ENCRYPTED_PASSWORD_SQ, null);
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
        prefs.edit().putString(PREF_ENCRYPTED_PASSWORD_SQ, Base64.encodeToString(encryptedPass, Base64.DEFAULT)).apply();
    }

    /*
        This method returns the proper cipher for use in getting and setting the encrypted password.
     */
    private Cipher getCipher(int mode, char[] securityAnswer, String securityQuestion) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2withHmacSHA1");
        KeySpec spec = new PBEKeySpec(
                securityAnswer,
                securityQuestion.getBytes(),
                65536,
                128
        );

        Key key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(mode, key);
            setSecurityIv(cipher.getIV());
        } else if (mode == Cipher.DECRYPT_MODE) {
            cipher.init(mode, key, new IvParameterSpec(getSecurityIv()));
        }

        return cipher;
    }

    private void setSecurityIv(byte[] iv) {
        prefs.edit().putString(PREF_SECURITY_IV, Base64.encodeToString(iv, Base64.DEFAULT)).apply();
    }

    private byte[] getSecurityIv() {
        return Base64.decode(prefs.getString(PREF_SECURITY_IV, null), Base64.DEFAULT);
    }

}
