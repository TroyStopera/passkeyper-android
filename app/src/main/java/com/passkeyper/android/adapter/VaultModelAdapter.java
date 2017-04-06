package com.passkeyper.android.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.passkeyper.android.vaultmodel.VaultModel;
import com.passkeyper.android.view.VaultModelEditView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class to be used for adapting VaultModel data.
 */
public abstract class VaultModelAdapter<K extends VaultModel> extends BaseAdapter implements VaultModelEditView.OnDeletePressedListener {

    protected final Context context;

    private final List<K> content = new LinkedList<>();
    private VaultModelEditView.OnDeletePressedListener listener;

    VaultModelAdapter(Context context) {
        this.context = context;
    }

    public final void setOnDeletePressedListener(VaultModelEditView.OnDeletePressedListener listener) {
        this.listener = listener;
    }

    public final K[] getAllVaultModels() {
        return content.toArray(newArray(content.size()));
    }

    public final void addVaultModels(Collection<K> models) {
        content.addAll(models);
        notifyDataSetChanged();
    }

    public final void addVaultModel(K model) {
        if (model != null) {
            content.add(model);
            notifyDataSetChanged();
        }
    }

    public final void addVaultModel(int index, K model) {
        if (index < 0) addVaultModel(model);
        else {
            content.add(index, model);
            notifyDataSetChanged();
        }
    }

    public final int remove(K model) {
        int index = content.indexOf(model);
        if (index >= 0) remove(index);
        return index;
    }

    public final K remove(int index) {
        K model = content.remove(index);
        notifyDataSetChanged();
        return model;
    }

    @Override
    public final void onDeletePressed(VaultModelEditView view) {
        if (listener != null)
            listener.onDeletePressed(view);
    }

    @Override
    public final int getCount() {
        return content.size();
    }

    @Override
    public final K getItem(int i) {
        return content.get(i);
    }

    @Override
    public final long getItemId(int i) {
        return getItem(i).getId();
    }

    protected abstract K[] newArray(int size);

}
