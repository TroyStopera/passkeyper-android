package com.passkeyper.android.crypto;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Contains methods that handle encryption/decryption for the local vault database.
 */
public class LocalDbCrypto {

    //TODO: Take into account if a user's phone upgrades from API 22 to 23 (data would become unreadable)

    /* used in API 23+ implementation */
    private static final String ALIAS_23 = "localDbKey23";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    /* used in legacy implementation */
    private static final String ALIAS_LEGACY = "localDbKeyLegacy";
    private static final String ALGORITHM_LEGACY = "RSA";
    /* used in both API 23+ and legacy */
    private static final String KEY_STORE = "AndroidKeyStore";
    private static final String FIXED_IV = "ReallyGreatInitializationVector";

    /**
     * Encrypts the given char[] securely for storage in the database.
     *
     * @param string the char[] to encrypt.
     * @return an encrypted String.
     * @throws Exception when 'string' could not be encrypted.
     */
    public static String encrypt(char[] string) throws Exception {
        Cipher cipher;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            cipher = getCipher23(Cipher.ENCRYPT_MODE);
        else
            cipher = getCipherLegacy(Cipher.ENCRYPT_MODE);

        //change chars to bytes
        final byte[] stringBytes = ArrayHelper.charsToBytes(string);
        //encode the string
        final char[] encodedChars = ArrayHelper.bytesToChars(cipher.doFinal(stringBytes));

        //clear sensitive data
        Arrays.fill(stringBytes, (byte) 0);

        return new String(encodedChars);
    }

    /**
     * Decrypts a String that was stored in the database.
     *
     * @param string the encrypted String to decrypt.
     * @return a decrypted char[].
     * @throws Exception when 'string' could not be decrypted.
     */
    public static char[] decrypt(String string) throws Exception {
        Cipher cipher;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            cipher = getCipher23(Cipher.DECRYPT_MODE);
        else
            cipher = getCipherLegacy(Cipher.DECRYPT_MODE);

        //decode the string to bytes then transform it into chars
        final byte[] decodedBytes = cipher.doFinal(ArrayHelper.charsToBytes(string.toCharArray()));
        final char[] decodedChars = ArrayHelper.bytesToChars(decodedBytes);

        //clear sensitive data
        Arrays.fill(decodedBytes, (byte) 0);

        return decodedChars;
    }

    /*
        API LEVEL 23+ IMPLEMENTATION
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static Cipher getCipher23(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(
                mode,
                getKey23(),
                new GCMParameterSpec(128, FIXED_IV.getBytes())
        );
        return cipher;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getKey23() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //if the key exists return it, otherwise generate a new one
        return keyStore.containsAlias(ALIAS_23) ? (SecretKey) keyStore.getKey(ALIAS_23, null) : genKey23();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey genKey23() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_STORE);
        keyGen.init(new KeyGenParameterSpec.Builder(
                ALIAS_23,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(false)
                .build()
        );
        return keyGen.generateKey();
    }

    /*
        LEGACY IMPLEMENTATION
     */

    private static Cipher getCipherLegacy(int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM_LEGACY);
        cipher.init(
                mode,
                mode == Cipher.DECRYPT_MODE ? getPrivateKeyLegacy() : getPublicKeyLegacy()
        );
        return cipher;
    }

    private static PrivateKey getPrivateKeyLegacy() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //if the key exists return it, otherwise generate a new one
        return keyStore.containsAlias(ALIAS_LEGACY) ?
                (PrivateKey) keyStore.getKey(ALIAS_LEGACY, null) : genKeyLegacy().getPrivate();
    }

    private static PublicKey getPublicKeyLegacy() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //if the key exists return it, otherwise generate a new one
        return keyStore.containsAlias(ALIAS_LEGACY) ?
                keyStore.getCertificate(ALIAS_LEGACY).getPublicKey() : genKeyLegacy().getPublic();
    }

    private static KeyPair genKeyLegacy() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_LEGACY, KEY_STORE);
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

}
