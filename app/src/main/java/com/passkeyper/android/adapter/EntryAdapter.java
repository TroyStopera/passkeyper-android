package com.passkeyper.android.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.view.EntryRecordViewHolder;

/**
 * Class used to adapt EntryRecord data for use in the main activity's RecyclerView.
 */
public class EntryAdapter extends RecyclerView.Adapter<EntryRecordViewHolder> {

    public enum SortOrder {
        Alpha, Alpha_Inverse, Chron, Chron_Inverse
    }

    private final Context context;
    private final VaultManager vaultManager;
    private final SortedList<EntryRecord> records = new SortedList<>(EntryRecord.class, new Callback());

    private OnClickListener listener;
    private long mExpandedId = -1;
    private SortOrder sortOrder = SortOrder.Chron;

    public EntryAdapter(Context context) {
        this.context = context;

        vaultManager = VaultManager.get(context);
        records.addAll(vaultManager.getAllEntryRecords());
    }

    public void collapseSelected() {
        mExpandedId = -1;
        notifyDataSetChanged();
    }

    public void remove(EntryRecord record) {
        records.remove(record);
    }

    public void add(EntryRecord record) {
        records.add(record);
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public EntryRecordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EntryRecordViewHolder(context, parent);
    }

    @Override
    public void onBindViewHolder(EntryRecordViewHolder holder, final int position) {
        final EntryRecord record = records.get(position);
        final boolean isExpanded = record.getId() == mExpandedId;

        holder.bind(record);
        holder.setExpanded(isExpanded);
        holder.setOnDeleteCLickListener(listener);
        holder.setOnEditCLickListener(listener);

        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedId = isExpanded ? -1 : record.getId();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public interface OnClickListener {

        void onEditClicked(EntryRecord record);

        void onDeleteClicked(EntryRecord record);

    }

    private class Callback extends SortedList.Callback<EntryRecord> {

        @Override
        public int compare(EntryRecord entryRecord1, EntryRecord entryRecord2) {
            switch (sortOrder) {
                case Alpha:
                    return entryRecord1.getAccount().compareTo(entryRecord2.getAccount());
                case Alpha_Inverse:
                    return 0 - entryRecord1.getAccount().compareTo(entryRecord2.getAccount());
                case Chron:
                    return Long.compare(entryRecord1.getId(), entryRecord2.getId());
                case Chron_Inverse:
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
