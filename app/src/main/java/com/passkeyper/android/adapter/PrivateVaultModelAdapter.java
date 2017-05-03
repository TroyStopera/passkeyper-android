package com.passkeyper.android.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.passkeyper.android.vaultmodel.PrivateModel;
import com.passkeyper.android.view.PrivateVaultModelEditView;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Abstract class to be used for adapting private VaultModel data.
 */
public abstract class PrivateVaultModelAdapter<K extends PrivateModel> extends BaseAdapter implements PrivateVaultModelEditView.OnDeletePressedListener {

    protected final Context context;

    private final List<K> content = new LinkedList<>();
    private final Set<Long> modelIds = new HashSet<>();
    private PrivateVaultModelEditView.OnDeletePressedListener listener;

    PrivateVaultModelAdapter(Context context) {
        this.context = context;
    }

    public final void setOnDeletePressedListener(PrivateVaultModelEditView.OnDeletePressedListener listener) {
        this.listener = listener;
    }

    public final K[] getAllVaultModels() {
        return content.toArray(newArray(content.size()));
    }

    public final void addNewVaultModels(Collection<K> models) {
        for (K model : models) {
            if (!modelIds.contains(model.getId()))
                addVaultModel(model);
        }
    }

    public final void addVaultModels(Collection<K> models) {
        content.addAll(models);
        for (K k : models)
            modelIds.add(k.getId());
        notifyDataSetChanged();
    }

    public final void addVaultModel(K model) {
        if (model != null) {
            content.add(model);
            modelIds.add(model.getId());
            notifyDataSetChanged();
        }
    }

    public final void addVaultModel(int index, K model) {
        if (index < 0) addVaultModel(model);
        else {
            content.add(index, model);
            modelIds.add(model.getId());
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
        modelIds.remove(model.getId());
        notifyDataSetChanged();
        return model;
    }

    @Override
    public final void onDeletePressed(PrivateVaultModelEditView view) {
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
