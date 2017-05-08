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

    private final View view;
    private final String descriptionSingle, descriptionMultiple;
    private final SnackBarDeleteListener<T> deleteListener;
    private final List<T> tList = new LinkedList<>();

    private Snackbar snackbar;
    private int currentUndoCount = 0;

    /**
     * Creates a new SnackbarUndoDelete.
     *
     * @param view                the view used int he Snackbar.
     * @param descriptionSingle   a String that is used to describe a single deleted item.
     * @param descriptionMultiple a String used to describe multiple deleted items.
     * @param deleteListener      the listener that is called when items are deleted or undone.
     */
    public SnackbarUndoDelete(View view, String descriptionSingle, String descriptionMultiple, SnackBarDeleteListener<T> deleteListener) {
        this.view = view;
        this.descriptionSingle = descriptionSingle;
        this.descriptionMultiple = descriptionMultiple;
        this.deleteListener = deleteListener;
    }

    /**
     * Closes the snackbar leading to onDelete being called for all current items.
     */
    public void forceDismissSnackbar() {
        if (snackbar != null)
            snackbar.dismiss();
    }

    /**
     * Adds a deleted item that can be undone.
     *
     * @param t the item to add.
     */
    public void addUndoable(T t) {
        if (snackbar != null) {
            snackbar.removeCallback(this);
            snackbar.dismiss();
        }
        currentUndoCount++;
        if (currentUndoCount == 1)
            snackbar = Snackbar.make(view, currentUndoCount + " " + descriptionSingle, Snackbar.LENGTH_LONG);
        else
            snackbar = Snackbar.make(view, currentUndoCount + " " + descriptionMultiple, Snackbar.LENGTH_LONG);

        snackbar.setAction(R.string.action_undo, v -> {
            snackbar.removeCallback(SnackbarUndoDelete.this);
            if (deleteListener != null) {
                deleteListener.onUndo(tList);
            }
            tList.clear();
            currentUndoCount = 0;
        });
        snackbar.addCallback(this);
        tList.add(t);

        //hide keyboard on delete so "undo" can be seen
        InputMethodManager inputMethodManager = (InputMethodManager) view.getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

        snackbar.show();
    }

    @Override
    public void onDismissed(Snackbar transientBottomBar, int event) {
        if (deleteListener != null) {
            deleteListener.onDelete(tList);
        }
        tList.clear();
        currentUndoCount = 0;
    }

    /**
     * Listener interface used to know when items are deleted or undone.
     *
     * @param <T> the type of items that are being deleted/undone.
     */
    public interface SnackBarDeleteListener<T> {

        void onDelete(Collection<T> tCollection);

        void onUndo(Collection<T> tCollection);

    }

}
