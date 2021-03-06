package com.passkeyper.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.passkeyper.android.activity.AbstractLoginActivity;
import com.passkeyper.android.activity.LocalSetupActivity;
import com.passkeyper.android.activity.LocalSignInActivity;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vault.local.DatabaseAuthException;
import com.passkeyper.android.vault.local.LocalVaultManager;

/**
 * Singleton class that handles variables that are needed globally.
 */
public class Vault {

    public static final int LOCAL_VAULT = 1;

    public static final String ACTIVITY_AFTER_SIGN_IN_EXTRA = "afterSignIn";
    private static final String TAG = "Vault";

    private static Vault vault;
    private VaultManager vaultManager;

    private Vault() {
        /* uses singleton pattern */
    }

    /**
     * Shows the sign in Activity on top of the current Activity for user authentication.
     *
     * @param activity the Activity the request is being made from.
     */
    public void requestSignIn(Activity activity) {
        Intent intent = new Intent(activity, getLoginClass(activity));
        activity.startActivity(intent);
    }

    /**
     * Shows the sign in Activity for user authentication.
     *
     * @param activity            the Activity the request is being made from.
     * @param activityAfterSignIn the Activity to start after authentication.
     */
    public void requestSignIn(Activity activity, @NonNull Class<?> activityAfterSignIn) {
        Intent intent = new Intent(activity, getLoginClass(activity));
        intent.putExtra(ACTIVITY_AFTER_SIGN_IN_EXTRA, activityAfterSignIn.getCanonicalName());
        activity.startActivity(intent);
    }

    /**
     * Signs the user out and closes the VaultManger. Does not show the sign in Activity.
     */
    public void signOut() {
        if (vaultManager != null && !vaultManager.isClosed())
            vaultManager.close();
        vaultManager = null;
    }

    /**
     * Attempts to authenticate the user in order to access the vault.
     *
     * @param context  an instance of Context used to load the vault.
     * @param password the password for the local vault.
     * @return true if the authentication was successful.
     */
    public boolean signIn(Context context, char[] password) {
        if (vaultManager != null && !vaultManager.isClosed())
            vaultManager.close();

        try {
            switch (new UserPreferences(context).getVaultManagerType()) {
                case LOCAL_VAULT:
                default:
                    vaultManager = new LocalVaultManager(context, password);
            }
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
        return vaultManager != null && !vaultManager.isClosed();
    }

    /**
     * @return an instance of a usable (i.e. not closed) VaultManager.
     */
    public VaultManager getManager() {
        return vaultManager;
    }

    /**
     * @return the instance of the singleton object.
     */
    public static Vault get() {
        if (vault == null)
            vault = new Vault();
        return vault;
    }

    private Class<? extends AbstractLoginActivity> getLoginClass(Context context) {
        if (LocalVaultManager.isLocalDbSetup(context)) {
            return LocalSignInActivity.class;
        } else {
            switch (new UserPreferences(context).getVaultManagerType()) {
                case LOCAL_VAULT:
                default:
                    return LocalSetupActivity.class;
            }
        }
    }

}
