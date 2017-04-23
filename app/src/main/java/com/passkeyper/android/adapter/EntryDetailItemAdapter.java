package com.passkeyper.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.passkeyper.android.vaultmodel.PrivateModel;
import com.passkeyper.android.view.EntryRecordDetailItemView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class used to adapt PrivateModel data for display within the main activity.
 */
public class EntryDetailItemAdapter<T extends PrivateModel> extends BaseAdapter {

    private final Context context;
    private final List<T> contents = new ArrayList<>();

    public EntryDetailItemAdapter(Context context, Collection<T> contents) {
        this.context = context;
        this.contents.addAll(contents);
        notifyDataSetChanged();
    }

    public void freeAllModels() {
        for (T t : contents)
            t.free();
    }

    @Override
    public int getCount() {
        return contents.size();
    }

    @Override
    public T getItem(int i) {
        return contents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        EntryRecordDetailItemView itemView = view instanceof EntryRecordDetailItemView
                ? (EntryRecordDetailItemView) view
                : new EntryRecordDetailItemView(context);

        itemView.setModel(getItem(i));
        return itemView;
    }

}
