package com.passkeyper.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.view.SensitiveEntryEditView;

/**
 * Class used to adapt/format SensitiveEntry objects.
 */
public class SensitiveEntryAdapter extends PrivateVaultModelAdapter<SensitiveEntry> {

    public SensitiveEntryAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SensitiveEntryEditView editView = new SensitiveEntryEditView(context, getItem(i));
        editView.setOnDeletePressedListener(this);

        if (i >= getCount() - 1) editView.setImeDone();

        return editView;
    }

    @Override
    protected SensitiveEntry[] newArray(int size) {
        return new SensitiveEntry[size];
    }

}
