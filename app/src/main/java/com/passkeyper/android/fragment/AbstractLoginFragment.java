package com.passkeyper.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;

/**
 * An abstract Fragment used in the LoginActivity.
 */
public abstract class AbstractLoginFragment extends Fragment {

    final Vault vault = Vault.get();
    LinearLayout window;

    LoginFragmentActivity loginFragmentActivity;

    abstract View onCreateWindowView(LayoutInflater inflater, @Nullable ViewGroup container);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LoginFragmentActivity)
            loginFragmentActivity = (LoginFragmentActivity) context;
        else
            throw new IllegalStateException("LoginFragment attached to non-LoginFragmentActivity Context");
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_container, container, false);

        window = (LinearLayout) view.findViewById(R.id.login_window);
        window.addView(onCreateWindowView(inflater, window));

        return view;
    }

    /**
     * The interface that the LoginActivity must implement in order for communication between Fragment and Activity.
     */
    public interface LoginFragmentActivity {

        void replace(AbstractLoginFragment fragment);

        void redirectAndFinish();

    }

}
