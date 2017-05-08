package com.passkeyper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.passkeyper.android.activity.LocalSignInActivity;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vault.local.DatabaseAuthException;
import com.passkeyper.android.vault.local.LocalVaultManager;

/**
 * Singleton class that handles variables that are needed globally.
 */
public class Vault {

    public static final String ACTIVITY_AFTER_SIGN_IN_EXTRA = "afterSignIn";
    private static final String TAG = "Vault";

    private static Vault mInstance;
    private VaultManager mVaultManager;

    private Vault() {
        /* uses singleton pattern */
    }

    /**
     * Shows the sign in Activity on top of the current Activity for user authentication.
     *
     * @param activity the Activity the request is being made from.
     */
    public void requestSignIn(Activity activity) {
        Intent intent = new Intent(activity, LocalSignInActivity.class);
        activity.startActivity(intent);
    }

    /**
     * Shows the sign in Activity for user authentication.
     *
     * @param activity            the Activity the request is being made from.
     * @param activityAfterSignIn the Activity to start after authentication.
     */
    public void requestSignIn(Activity activity, Class<?> activityAfterSignIn) {
        Intent intent = new Intent(activity, LocalSignInActivity.class);
        intent.putExtra(ACTIVITY_AFTER_SIGN_IN_EXTRA, activityAfterSignIn.getCanonicalName());
        activity.startActivity(intent);
    }

    /**
     * Signs the user out and closes the VaultManger. Does not show the sign in Activity.
     */
    public void signOut() {
        if (mVaultManager != null && !mVaultManager.isClosed())
            mVaultManager.close();
        mVaultManager = null;
    }

    /**
     * Attempts to authenticate the user in order to access the local vault.
     *
     * @param context  an instance of Context used to load the vault.
     * @param password the password for the local vault.
     * @return true if the authentication was successful.
     */
    public boolean signInToLocalVault(Context context, char[] password) {
        if (mVaultManager != null && !mVaultManager.isClosed())
            mVaultManager.close();

        try {
            mVaultManager = new LocalVaultManager(context, password);
            return true;
        } catch (DatabaseAuthException e) {
            Log.w(TAG, "Unable to login", e);
            return false;
        }
    }

    /**
     * Attempts to load a VaultManager without the user signing in.
     *
     * @return true if the VaultManager was loaded.
     */
    @SuppressWarnings("SameReturnValue")
    public boolean loadManager() {
        return false;
    }

    /**
     * @return true if there is a usable VaultManager.
     */
    public boolean hasManager() {
        return mVaultManager != null && !mVaultManager.isClosed();
    }

    /**
     * @return an instance of a usable (i.e. not closed) VaultManager.
     */
    public VaultManager getManager() {
        return mVaultManager;
    }

    /**
     * @return the instance of the singleton object.
     */
    public static Vault get() {
        if (mInstance == null)
            mInstance = new Vault();
        return mInstance;
    }

}
