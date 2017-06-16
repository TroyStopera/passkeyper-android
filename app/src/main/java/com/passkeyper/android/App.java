package com.passkeyper.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

/**
 * Application class that is used to monitor when the user has closed the app or when the screen has timed out.
 */
public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private static final long AUTH_TIMEOUT = 60000;

    private final Handler handler = new Handler();
    private final Runnable signOutRunnable = () -> Vault.get().signOut();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        handler.removeCallbacks(signOutRunnable);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        handler.removeCallbacks(signOutRunnable);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        handler.removeCallbacks(signOutRunnable);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        //only start sign out timer if there is a VaultManager
        if (Vault.get().hasManager()) {
            handler.postDelayed(signOutRunnable, AUTH_TIMEOUT);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        /* NOT NEEDED */
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        /* NOT NEEDED */
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        /* NOT NEEDED */
    }

}
