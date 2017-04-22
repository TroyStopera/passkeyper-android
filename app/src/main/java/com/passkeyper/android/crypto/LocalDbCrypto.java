package com.passkeyper.android.crypto;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.util.Base64;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.security.auth.x500.X500Principal;

/**
 * Contains methods that handle encryption/decryption for the local vault database.
 */
public class LocalDbCrypto {

    //TODO: Take into account if a user's phone upgrades from API 22 to 23 (data would become unreadable)

    /* used in API 23+ implementation */
    private static final String ALIAS = "localDbKey23";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS7Padding";
    private static final String ALGORITHM = "AES";
    private static final String FIXED_IV = "com.passkeyper.android.crypto.fixed.iv";
    /* used in legacy implementation */
    private static final String ALIAS_LEGACY = "localDbKeyLegacy";
    private static final String CIPHER_ALGORITHM_LEGACY = "RSA/ECB/PKCS1Padding";
    private static final String ALGORITHM_LEGACY = "RSA";
    /* used in both API 23+ and legacy */
    private static final String KEY_STORE = "AndroidKeyStore";

    /**
     * Encrypts the given char[] securely for storage in the database.
     *
     * @param context an instance of Context from the app
     * @param string  the char[] to encrypt.
     * @return an encrypted String.
     * @throws Exception when 'string' could not be encrypted.
     */
    public static String encrypt(Context context, char[] string) throws Exception {
        Cipher cipher;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            cipher = getCipher23(Cipher.ENCRYPT_MODE);
        else cipher = getCipherLegacy(context, Cipher.ENCRYPT_MODE);

        //change chars to bytes
        final byte[] stringBytes = ArrayHelper.charsToBytes(string);
        //encode the string
        final byte[] base64 = Base64.encode(cipher.doFinal(stringBytes), Base64.DEFAULT);

        //clear sensitive data
        Arrays.fill(stringBytes, (byte) 0);

        return new String(base64);
    }

    /**
     * Decrypts a String that was stored in the database.
     *
     * @param context an instance of Context from the app
     * @param string  the encrypted String to decrypt.
     * @return a decrypted char[].
     * @throws Exception when 'string' could not be decrypted.
     */
    public static char[] decrypt(Context context, String string) throws Exception {
        Cipher cipher;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            cipher = getCipher23(Cipher.DECRYPT_MODE);
        else cipher = getCipherLegacy(context, Cipher.DECRYPT_MODE);

        //decode the string to bytes then transform it into chars
        final byte[] base64 = Base64.decode(string.getBytes(), Base64.DEFAULT);
        final byte[] decodedBytes = cipher.doFinal(base64);
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
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(
                mode,
                getKey23(),
                new IvParameterSpec(FIXED_IV.getBytes())
        );
        return cipher;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getKey23() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //if the key exists return it, otherwise generate a new one
        return keyStore.containsAlias(ALIAS) ? (SecretKey) keyStore.getKey(ALIAS, null) : genKey23();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey genKey23() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM, KEY_STORE);
        keyGen.init(new KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setRandomizedEncryptionRequired(false)
                .build()
        );
        return keyGen.generateKey();
    }

    /*
        LEGACY IMPLEMENTATION
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private static Cipher getCipherLegacy(Context context, int mode) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_LEGACY);
        cipher.init(
                mode,
                mode == Cipher.DECRYPT_MODE ? getPrivateKeyLegacy(context) : getPublicKeyLegacy(context)
        );
        return cipher;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private static PrivateKey getPrivateKeyLegacy(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //generate the key if it doesn't exist
        if (!keyStore.containsAlias(ALIAS_LEGACY)) genKeyLegacy(context);

        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS_LEGACY, null);
        return entry.getPrivateKey();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private static PublicKey getPublicKeyLegacy(Context context) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEY_STORE);
        keyStore.load(null);

        //generate the key if it doesn't exist
        if (!keyStore.containsAlias(ALIAS_LEGACY)) genKeyLegacy(context);

        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS_LEGACY, null);
        return entry.getCertificate().getPublicKey();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private static void genKeyLegacy(Context context) throws Exception {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.YEAR, 30);

        KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                .setAlias(ALIAS_LEGACY)
                .setSubject(new X500Principal("CN=" + ALIAS_LEGACY))
                .setSerialNumber(BigInteger.TEN)
                .setStartDate(start.getTime())
                .setEndDate(end.getTime())
                .build();

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM_LEGACY, KEY_STORE);
        keyGen.initialize(spec);
        keyGen.generateKeyPair();
    }

}
