package com.passkeyper.android.adapter;

import android.app.Activity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.view.EntryRecordViewHolder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Class used to adapt EntryRecord data for use in the main activity's RecyclerView.
 */
public class EntryAdapter extends RecyclerView.Adapter<EntryRecordViewHolder> {

    /**
     * The order by which to sort entries in an EntryAdapter.
     */
    public enum SortOrder {
        AtoZ, ZtoA, OldestFirst, NewestFirst
    }

    private final Activity activity;
    private final SortedList<EntryRecord> list = new SortedList<>(EntryRecord.class, new Callback());
    /* contains all records that are removed from the list while a user searches */
    private final List<EntryRecord> filteredOut = new LinkedList<>();

    private VaultManager vaultManager;

    private OnEntryExpandedListener onEntryExpandedListener;
    private OnActionListener listener;

    private long expandedId = -1;
    private final SortOrder sortOrder;

    public EntryAdapter(Activity activity, VaultManager vaultManager, SortOrder sortOrder) {
        this.activity = activity;
        this.sortOrder = sortOrder;

        setVaultManager(vaultManager);
        reload();
    }

    public void setVaultManager(VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    public void reload() {
        list.clear();
        list.addAll(vaultManager.getAllEntryRecords());
        notifyDataSetChanged();
    }

    public boolean hasExpandedEntry() {
        return expandedId != -1;
    }

    public void collapseSelected() {
        expandedId = -1;
        notifyDataSetChanged();
    }

    public void remove(EntryRecord record) {
        list.remove(record);
    }

    public void add(EntryRecord record) {
        list.add(record);
    }

    public void addAll(Collection<EntryRecord> entryRecords) {
        list.addAll(entryRecords);
    }

    public void setOnEntryExpandedListener(OnEntryExpandedListener onEntryExpandedListener) {
        this.onEntryExpandedListener = onEntryExpandedListener;
    }

    public void setOnClickListener(OnActionListener listener) {
        this.listener = listener;
    }

    @Override
    public EntryRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EntryRecordViewHolder(activity, parent, vaultManager);
    }

    @Override
    public void onBindViewHolder(final EntryRecordViewHolder holder, final int position) {
        final EntryRecord record = list.get(position);
        final boolean isExpanded = record.getId() == expandedId;

        holder.bind(record);
        holder.setExpanded(isExpanded);
        holder.setOnDeleteCLickListener(listener);
        holder.setOnEditCLickListener(listener);

        holder.setOnClickListener(view -> {
            expandedId = isExpanded ? -1 : record.getId();
            if (!isExpanded && onEntryExpandedListener != null)
                onEntryExpandedListener.onEntryExpanded(holder.getAdapterPosition());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void showAll() {
        list.addAll(filteredOut);
        filteredOut.clear();
    }

    public void filter(String filterText) {
        filterText = filterText.toLowerCase();

        if (filterText.isEmpty()) {
            showAll();
        } else {
            list.beginBatchedUpdates();
            for (int i = list.size() - 1; i >= 0; i--) {
                EntryRecord record = list.get(i);
                if (!record.getAccount().toLowerCase().contains(filterText))
                    filteredOut.add(list.removeItemAt(i));
            }
            list.endBatchedUpdates();
        }
    }

    /**
     * Listener interface for detecting when an entry int he adapter is expanded.
     */
    public interface OnEntryExpandedListener {

        void onEntryExpanded(int pos);

    }

    /**
     * Listener interface for when the edit or delete icon buttons are pressed.
     */
    public interface OnActionListener {

        void onEditClicked(EntryRecord record);

        void onDeleteClicked(EntryRecord record);

    }

    /**
     * Implementation of the Callback needed to communicate from SortedList to Adapter.
     */
    private class Callback extends SortedList.Callback<EntryRecord> {

        @Override
        public int compare(EntryRecord entryRecord1, EntryRecord entryRecord2) {
            switch (sortOrder) {
                case AtoZ:
                    return entryRecord1.getAccount().compareTo(entryRecord2.getAccount());
                case ZtoA:
                    return 0 - entryRecord1.getAccount().compareTo(entryRecord2.getAccount());
                case OldestFirst:
                    return Long.compare(entryRecord1.getId(), entryRecord2.getId());
                case NewestFirst:
                    return 0 - Long.compare(entryRecord1.getId(), entryRecord2.getId());
                default:
                    return entryRecord1.getAccount().compareTo(entryRecord2.getAccount());
            }
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(EntryRecord oldItem, EntryRecord newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areItemsTheSame(EntryRecord item1, EntryRecord item2) {
            return item1.getId() == item2.getId();
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }
    }

}
