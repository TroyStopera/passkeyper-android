package com.passkeyper.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.activity.AbstractLoginActivity;

/**
 * An abstract Fragment used in the LoginActivity.
 */
public abstract class AbstractLoginFragment<T extends AbstractLoginActivity> extends Fragment {

    final Vault vault = Vault.get();
    LinearLayout window;
    Animation shake;

    T loginFragmentActivity;

    abstract View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            //noinspection unchecked
            loginFragmentActivity = (T) context;
            shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        } catch (Exception ex) {
            throw new IllegalStateException("LoginFragment attached to non-LoginFragmentActivity Context", ex);
        }
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_container, container, false);

        window = (LinearLayout) view.findViewById(R.id.login_window);
        window.addView(onCreateWindowView(inflater, window));

        return view;
    }

}
