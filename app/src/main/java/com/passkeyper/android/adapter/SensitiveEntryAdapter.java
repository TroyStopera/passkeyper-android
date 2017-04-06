package com.passkeyper.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.view.SensitiveEntryEditView;

/**
 * Class used to adapt/format SensitiveEntry objects.
 */
public class SensitiveEntryAdapter extends VaultModelAdapter<SensitiveEntry> {

    public SensitiveEntryAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SensitiveEntryEditView editView = new SensitiveEntryEditView(context, getItem(i));
        editView.setOnDeletePressedListener(this);

        if (i >= getCount() - 1) editView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        return editView;
    }

    @Override
    protected SensitiveEntry[] newArray(int size) {
        return new SensitiveEntry[size];
    }

}
