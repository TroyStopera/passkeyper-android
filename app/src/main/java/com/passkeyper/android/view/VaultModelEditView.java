package com.passkeyper.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.VaultModel;

/**
 * A custom View that encapsulates the functionality needed to edit a VaultModel.
 */
public abstract class VaultModelEditView<T extends VaultModel> extends FrameLayout implements View.OnClickListener {

    protected static final TransformationMethod hidden = new PasswordTransformationMethod();
    private static int lastId = 0;

    protected T model;
    private ImageButton visibilityButton, deleteButton, doneButton;
    private boolean isTextCensored = true, isInEditMode = false;
    private OnDeletePressedListener listener;
    private final int uniqueId = lastId++;

    public VaultModelEditView(@NonNull Context context, int layoutRes, T model) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(layoutRes, this);

        this.model = model;
        setUiFields();
        updateUi();
    }

    public VaultModelEditView(@NonNull Context context, @Nullable AttributeSet attrs, int layoutRes, T model) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(layoutRes, this);

        this.model = model;
        setUiFields();
        updateUi();
    }

    public final int getUniqueId() {
        return uniqueId;
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
        if (obj instanceof VaultModelEditView) {
            VaultModelEditView view = (VaultModelEditView) obj;
            return view.getUniqueId() == getUniqueId();
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

    protected void setEditMode(boolean enabled) {
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
        visibilityButton = (ImageButton) findViewById(R.id.edit_visible);
        deleteButton = (ImageButton) findViewById(R.id.edit_delete);
        doneButton = (ImageButton) findViewById(R.id.edit_done);

        visibilityButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);

        onInitUiFields();
    }

    public interface OnDeletePressedListener {

        void onDeletePressed(VaultModelEditView view);

    }

}
