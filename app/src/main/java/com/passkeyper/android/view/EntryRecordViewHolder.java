package com.passkeyper.android.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.passkeyper.android.R;
import com.passkeyper.android.adapter.EntryAdapter;
import com.passkeyper.android.adapter.EntryDetailItemAdapter;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;

/**
 * ViewHolder used to display EntryRecords and their sub-items.
 */
public class EntryRecordViewHolder extends RecyclerView.ViewHolder {

    private final Context context;
    private final VaultManager vaultManager;

    private final LinearLayout detailLayout;
    private final TextView accountView, usernameView;
    private final ListView sensitiveDataList, securityQuestionsList;
    private final ImageButton editButton, deleteButton;

    private EntryDetailItemAdapter sensitiveDataAdapter, securityQuestionAdapter;
    private EntryRecord record;

    public EntryRecordViewHolder(Context context, ViewGroup parent, VaultManager vaultManager) {
        super(LayoutInflater.from(context).inflate(R.layout.view_entry_record, parent, false));
        this.context = context;
        this.vaultManager = vaultManager;

        detailLayout = (LinearLayout) itemView.findViewById(R.id.entry_details);
        accountView = (TextView) itemView.findViewById(R.id.record_account);
        usernameView = (TextView) itemView.findViewById(R.id.record_username);
        sensitiveDataList = (ListView) itemView.findViewById(R.id.record_sensitive_data_list);
        securityQuestionsList = (ListView) itemView.findViewById(R.id.record_security_question_list);
        editButton = (ImageButton) itemView.findViewById(R.id.record_edit);
        deleteButton = (ImageButton) itemView.findViewById(R.id.record_delete);

        detailLayout.setVisibility(View.GONE);
    }

    public void bind(EntryRecord record) {
        this.record = record;

        accountView.setText(record.getAccount());
        usernameView.setText(record.getUsername());
    }

    public void setExpanded(boolean isExpanded) {
        if (record == null) return;
        //always ensure that any private info is cleared before losing reference to the adapters
        if (sensitiveDataAdapter != null) sensitiveDataAdapter.freeAllModels();
        if (securityQuestionAdapter != null) securityQuestionAdapter.freeAllModels();

        if (isExpanded) {
            sensitiveDataAdapter = new EntryDetailItemAdapter<>(context, vaultManager.getSensitiveEntries(record));
            sensitiveDataList.setAdapter(sensitiveDataAdapter);
            securityQuestionAdapter = new EntryDetailItemAdapter<>(context, vaultManager.getSecurityQuestions(record));
            securityQuestionsList.setAdapter(securityQuestionAdapter);

            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
            detailLayout.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            detailLayout.setVisibility(View.GONE);
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void setOnEditCLickListener(final EntryAdapter.OnActionListener listener) {
        editButton.setOnClickListener(view -> {
            if (listener != null)
                listener.onEditClicked(record);
        });
    }

    public void setOnDeleteCLickListener(final EntryAdapter.OnActionListener listener) {
        deleteButton.setOnClickListener(view -> {
            if (listener != null)
                listener.onDeleteClicked(record);
        });
    }

}
