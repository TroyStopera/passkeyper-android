package com.passkeyper.android.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.SensitiveEntry;

/**
 * A custom View that encapsulates the functionality needed to edit a SensitiveEntry.
 */
@SuppressLint("ViewConstructor")
public class SensitiveEntryEditView extends PrivateVaultModelEditView<SensitiveEntry> implements View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private Spinner nameSpinner;
    private TextInputLayout inputLayout;
    private TextInputEditText valueEditText;

    public SensitiveEntryEditView(@NonNull Context context, SensitiveEntry entry) {
        super(context, R.layout.view_edit_sensitive, entry);
    }

    public void setImeDone() {
        valueEditText.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.edit_sensitive_edit_text)
            setEditMode(hasFocus);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        inputLayout.setHint((String) nameSpinner.getSelectedItem());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        nameSpinner.setSelection(0);
    }

    @Override
    protected void onEditEnabled() {
        nameSpinner.setVisibility(VISIBLE);
    }

    @Override
    protected void onEditDisabled() {
        nameSpinner.setVisibility(GONE);
        valueEditText.clearFocus();
    }

    @Override
    protected void onCensoredChange(boolean b) {
        if (b) valueEditText.setTransformationMethod(hidden);
        else valueEditText.setTransformationMethod(null);
    }

    @Override
    protected void onInitUiFields() {
        nameSpinner = (Spinner) findViewById(R.id.edit_sensitive_name);
        inputLayout = (TextInputLayout) findViewById(R.id.edit_sensitive_input);
        valueEditText = (TextInputEditText) findViewById(R.id.edit_sensitive_edit_text);

        nameSpinner.setOnItemSelectedListener(this);
        inputLayout.setHint((String) nameSpinner.getSelectedItem());
        valueEditText.setOnFocusChangeListener(this);
    }

    @Override
    protected void onWriteToModel() {
        int len = valueEditText.length();
        final char[] chars = new char[len];
        //fill the array and return
        valueEditText.getText().getChars(0, len, chars, 0);
        model.setValue(chars);
        model.setName((String) nameSpinner.getSelectedItem());
    }

    @Override
    protected void updateUi() {
        if (model.hasName()) {
            inputLayout.setHint(model.getName());

            //noinspection unchecked
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) nameSpinner.getAdapter();

            if (adapter.getPosition(model.getName()) != -1)
                nameSpinner.setSelection(adapter.getPosition(model.getName()));
            else {
                adapter.add(model.getName());
                adapter.notifyDataSetChanged();
                nameSpinner.setSelection(adapter.getPosition(model.getName()));
            }
        }

        char[] value = model.getValue();
        if (model.hasValue())
            valueEditText.setText(value, 0, value.length);
    }

}
