package com.passkeyper.android;

import android.app.Application;

import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vault.local.LocalVaultManager;

/**
 * Application class that handles variables that are needed globally.
 */
public class AppVault extends Application {

    private static AppVault mInstance;
    private VaultManager mVaultManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        //TODO: consider which vault manager to use
        //TODO: get a real password
        mVaultManager = new LocalVaultManager(this, new char[]{'a', 'b', 'c'});
    }

    public VaultManager getManager() {
        return mVaultManager;
    }

    public static AppVault get() {
        return mInstance;
    }

}
