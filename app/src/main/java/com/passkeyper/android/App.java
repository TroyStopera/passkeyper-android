package com.passkeyper.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import com.passkeyper.android.prefs.AuthPreferences;
import com.passkeyper.android.vault.local.LocalVaultManager;

/**
 * Application class that is used to monitor when the user has closed the app or when the screen has timed out.
 */
public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private final Handler handler = new Handler();

    private final Runnable signOutRunnable = new Runnable() {
        @Override
        public void run() {
            Vault.get().signOut();
        }
    };

    private AuthPreferences authPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        authPreferences = AuthPreferences.get(this);
        registerActivityLifecycleCallbacks(this);

        //TODO: implement a true setup
        if (!LocalVaultManager.isLocalDbSetup(this))
            LocalVaultManager.setupLocalDb(this, new char[]{'a', 'b', 'c'});
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
            long timeout = authPreferences.getAppClosedAuthTimeout();
            /*
                To ensure that users are not signed out when simply switching activities within this
                app there must be at least a 1000ms delay before signing out.
                If a user had this preference set to 0 then when 'onPause' was called from MainActivity
                when clicking the edit button they may be signed out before 'onCreate' was called
                for EditEntryActivity.
            */
            if (timeout < 1000) timeout = 1000;
            handler.postDelayed(signOutRunnable, timeout);
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