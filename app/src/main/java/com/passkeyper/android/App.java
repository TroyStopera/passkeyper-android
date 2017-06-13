package com.passkeyper.android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

/**
 * Application class that is used to monitor when the user has closed the app or when the screen has timed out.
 */
public class App extends Application implements Application.ActivityLifecycleCallbacks {

    private final Handler handler = new Handler();

    private final Runnable signOutRunnable = () -> Vault.get().signOut();

    private UserPrefs userPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        userPrefs = new UserPrefs(this);
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
            long timeout = userPrefs.getAppClosedAuthTimeout();
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
