package com.passkeyper.android.adapter;

import android.app.Activity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.view.EntryRecordViewHolder;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Class used to adapt EntryRecord data for use in the main activity's RecyclerView.
 */
public class EntryAdapter extends RecyclerView.Adapter<EntryRecordViewHolder> implements SearchView.OnQueryTextListener {

    public enum SortOrder {
        AtoZ, ZtoA, OldestFirst, NewestFirst
    }

    private final Activity context;
    private final List<EntryRecord> allRecords = new LinkedList<>();
    private final SortedList<EntryRecord> list = new SortedList<>(EntryRecord.class, new Callback());

    private OnEntryExpandedListener onEntryExpandedListener;
    private OnActionListener listener;
    private long mExpandedId = -1;
    private SortOrder sortOrder = SortOrder.OldestFirst;

    public EntryAdapter(Activity context) {
        this.context = context;

        reload();
    }

    public void reload() {
        allRecords.clear();
        allRecords.addAll(VaultManager.get(context).getAllEntryRecords());
        list.clear();
        list.addAll(allRecords);
    }

    public boolean hasExpandedEntry() {
        return mExpandedId != -1;
    }

    public void collapseSelected() {
        mExpandedId = -1;
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
        return new EntryRecordViewHolder(context, parent);
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;

        //make a copy of the items
        Collection<EntryRecord> entryRecords = new LinkedList<>();
        for (int i = 0; i < list.size(); i++)
            entryRecords.add(list.get(i));

        //clear it and re-add them to force a re-sort
        list.clear();
        addAll(entryRecords);
    }

    @Override
    public void onBindViewHolder(final EntryRecordViewHolder holder, final int position) {
        final EntryRecord record = list.get(position);
        final boolean isExpanded = record.getId() == mExpandedId;

        holder.bind(record);
        holder.setExpanded(isExpanded);
        holder.setOnDeleteCLickListener(listener);
        holder.setOnEditCLickListener(listener);

        holder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mExpandedId = isExpanded ? -1 : record.getId();
                if (!isExpanded && onEntryExpandedListener != null)
                    onEntryExpandedListener.onEntryExpanded(record, holder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        //hide keyboard on search clicked
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(context.findViewById(android.R.id.content).getWindowToken(), 0);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        newText = newText.toLowerCase();
        if (newText.isEmpty()) {
            list.clear();
            list.addAll(allRecords);
            return true;
        }

        list.beginBatchedUpdates();
        for (int i = list.size() - 1; i >= 0; i--) {
            EntryRecord record = list.get(i);
            if (!record.getAccount().toLowerCase().contains(newText))
                list.removeItemAt(i);
        }
        list.endBatchedUpdates();

        return true;
    }

    /**
     * Listener interface for detecting when an entry int he adapter is expanded.
     */
    public interface OnEntryExpandedListener {

        void onEntryExpanded(EntryRecord record, int pos);

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
