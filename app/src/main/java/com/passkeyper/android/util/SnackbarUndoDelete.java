package com.passkeyper.android.util;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.passkeyper.android.R;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Class used to abstract the functionality of a Snackbar that has an undo feature.
 * Allows for the Snackbar to auto-update the number of deleted items that can be undone.
 */
public class SnackbarUndoDelete<T> extends Snackbar.Callback {

    private final View mView;
    private final String mDescriptionSingle, mDescriptionMultiple;
    private final SnackBarDeleteListener<T> mDeleteListener;
    private final List<T> tList = new LinkedList<>();

    private Snackbar mSnackbar;
    private int mCurrentUndoCount = 0;

    public SnackbarUndoDelete(View view, String descriptionSingle, String descriptionMultiple, SnackBarDeleteListener<T> deleteListener) {
        mView = view;
        mDescriptionSingle = descriptionSingle;
        mDescriptionMultiple = descriptionMultiple;
        mDeleteListener = deleteListener;
    }

    public void forceDismissSnackbar() {
        if (mSnackbar != null)
            mSnackbar.dismiss();
    }

    public void addUndoable(T t) {
        if (mSnackbar != null) {
            mSnackbar.removeCallback(this);
            mSnackbar.dismiss();
        }
        mCurrentUndoCount++;
        if (mCurrentUndoCount == 1)
            mSnackbar = Snackbar.make(mView, mCurrentUndoCount + " " + mDescriptionSingle, Snackbar.LENGTH_LONG);
        else
            mSnackbar = Snackbar.make(mView, mCurrentUndoCount + " " + mDescriptionMultiple, Snackbar.LENGTH_LONG);
        mSnackbar.setAction(R.string.action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.removeCallback(SnackbarUndoDelete.this);
                if (mDeleteListener != null) {
                    mDeleteListener.onUndo(tList);
                }
                tList.clear();
                mCurrentUndoCount = 0;
            }
        });
        mSnackbar.addCallback(this);
        tList.add(t);

        //hide keyboard on delete so "undo" can be seen
        InputMethodManager inputMethodManager = (InputMethodManager) mView.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mView.getWindowToken(), 0);

        mSnackbar.show();
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, int event) {
        if (mDeleteListener != null) {
            mDeleteListener.onDelete(tList);
        }
        tList.clear();
        mCurrentUndoCount = 0;
    }

    public interface SnackBarDeleteListener<T> {

        void onDelete(Collection<T> tCollection);

        void onUndo(Collection<T> tCollection);

    }

}
