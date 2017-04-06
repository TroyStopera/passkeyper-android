package com.passkeyper.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SensitiveEntry;

/**
 * A custom View that encapsulates the functionality needed to edit a SensitiveEntry.
 */
public class SensitiveEntryEditView extends FrameLayout implements View.OnFocusChangeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final TransformationMethod password = new PasswordTransformationMethod();

    private Spinner nameSpinner;
    private TextInputLayout inputLayout;
    private TextInputEditText valueEditText;
    private ImageButton visibilityButton, deleteButton, doneButton;

    private boolean isTextCensored = true;
    private OnDeletePressedListener listener;

    public SensitiveEntryEditView(@NonNull Context context) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_edit_sensitive, this);

        setUiFields();
    }

    public SensitiveEntryEditView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_edit_sensitive, this);

        setUiFields();
    }

    public SensitiveEntry getSensitiveEntry(EntryRecord record) {
        SensitiveEntry entry = new SensitiveEntry(record);
        entry.setName(getEntryName());
        entry.setValue(getValue());
        return entry;
    }

    public void setSensitiveEntry(SensitiveEntry entry) {
        inputLayout.setHint(entry.getName());
        valueEditText.setText(entry.getValue(), 0, entry.getValue().length);
    }

    public String getEntryName() {
        return (String) nameSpinner.getSelectedItem();
    }

    public char[] getValue() {
        int len = valueEditText.length();
        final char[] chars = new char[len];
        //fill the array and return
        valueEditText.getText().getChars(0, len, chars, 0);
        return chars;
    }

    public void setDeletePressedListener(OnDeletePressedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.edit_sensitive_edit_text)
            setEditMode(hasFocus);
    }

    @Override
    public void onClick(View view) {
        // send a delete pressed event
        if (view.getId() == R.id.edit_sensitive_delete) {
            if (listener != null) listener.onDeletePressed(this);
        }
        // hide edit mode
        else if (view.getId() == R.id.edit_sensitive_done) {
            setEditMode(false);
        }
        // toggle text visibility
        else if (view.getId() == R.id.edit_sensitive_visible) {
            if (isTextCensored) {
                visibilityButton.setImageResource(R.drawable.ic_visibility_on);
                valueEditText.setTransformationMethod(null);
            } else {
                visibilityButton.setImageResource(R.drawable.ic_visibility_off);
                valueEditText.setTransformationMethod(password);
            }
            isTextCensored = !isTextCensored;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        inputLayout.setHint((String) nameSpinner.getSelectedItem());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        nameSpinner.setSelection(0);
    }

    private void setUiFields() {
        nameSpinner = (Spinner) findViewById(R.id.edit_sensitive_name);
        inputLayout = (TextInputLayout) findViewById(R.id.edit_sensitive_input);
        valueEditText = (TextInputEditText) findViewById(R.id.edit_sensitive_edit_text);
        visibilityButton = (ImageButton) findViewById(R.id.edit_sensitive_visible);
        deleteButton = (ImageButton) findViewById(R.id.edit_sensitive_delete);
        doneButton = (ImageButton) findViewById(R.id.edit_sensitive_done);

        nameSpinner.setOnItemSelectedListener(this);
        inputLayout.setHint((String) nameSpinner.getSelectedItem());
        valueEditText.setOnFocusChangeListener(this);
        visibilityButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        doneButton.setOnClickListener(this);
    }

    private void setEditMode(boolean enabled) {
        if (enabled) {
            nameSpinner.setVisibility(VISIBLE);
            doneButton.setVisibility(VISIBLE);
            deleteButton.setVisibility(GONE);
        } else {
            nameSpinner.setVisibility(GONE);
            doneButton.setVisibility(GONE);
            deleteButton.setVisibility(VISIBLE);
            valueEditText.clearFocus();
        }
    }

    public interface OnDeletePressedListener {

        void onDeletePressed(SensitiveEntryEditView view);

    }

}
