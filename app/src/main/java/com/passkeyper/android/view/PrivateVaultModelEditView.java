package com.passkeyper.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.PrivateModel;

/**
 * A custom View that encapsulates the functionality needed to edit a private VaultModel.
 */
public abstract class PrivateVaultModelEditView<T extends PrivateModel> extends FrameLayout implements View.OnClickListener {

    static final TransformationMethod hidden = new PasswordTransformationMethod();
    private static int lastId = 0;

    private final int uniqueId = lastId++;

    private ImageButton visibilityButton, deleteButton, doneButton;

    private boolean isTextCensored = true, isInEditMode = false;

    final T model;
    private OnDeletePressedListener listener;

    PrivateVaultModelEditView(@NonNull Context context, int layoutRes, T model) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(layoutRes, this);

        this.model = model;
        setUiFields();
        updateUi();
    }

    @Override
    public void onClick(View view) {
        // send a delete pressed event
        if (view.getId() == R.id.edit_delete) {
            if (listener != null)
                listener.onDeletePressed(this);
        }
        // hide edit mode
        else if (view.getId() == R.id.edit_done) {
            setEditMode(false);
        }
        // toggle text visibility
        else if (view.getId() == R.id.edit_visible) {
            if (isTextCensored) {
                visibilityButton.setImageResource(R.drawable.ic_visibility_on);
                onCensoredChange(false);
            } else {
                visibilityButton.setImageResource(R.drawable.ic_visibility_off);
                onCensoredChange(true);
            }
            isTextCensored = !isTextCensored;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PrivateVaultModelEditView) {
            PrivateVaultModelEditView view = (PrivateVaultModelEditView) obj;
            return view.uniqueId == uniqueId;
        }
        return false;
    }

    public final void setOnDeletePressedListener(OnDeletePressedListener listener) {
        this.listener = listener;
    }

    public final T getVaultModel() {
        return model;
    }

    protected abstract void onWriteToModel();

    protected abstract void updateUi();

    protected abstract void onEditEnabled();

    protected abstract void onEditDisabled();

    protected abstract void onCensoredChange(boolean b);

    protected abstract void onInitUiFields();

    void setEditMode(boolean enabled) {
        if (enabled && !isInEditMode) {
            doneButton.setVisibility(VISIBLE);
            deleteButton.setVisibility(GONE);
            onEditEnabled();
            isInEditMode = true;
        } else if (!enabled && isInEditMode) {
            doneButton.setVisibility(GONE);
            deleteButton.setVisibility(VISIBLE);
            onEditDisabled();
            isInEditMode = false;
            //write any changes to the model
            onWriteToModel();
        }
    }

    private void setUiFields() {
        visibilityButton = findViewById(R.id.edit_visible);
        deleteButton = findViewById(R.id.edit_delete);
        doneButton = findViewById(R.id.edit_done);

        visibilityButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        onInitUiFields();
    }

    /**
     * Listener interface for when a user clicks to delete a private vault model.
     */
    public interface OnDeletePressedListener {

        void onDeletePressed(PrivateVaultModelEditView view);

    }

}
