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

import com.passkeyper.android.AppVault;
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
    private final LinearLayout mDetailLayout;
    private final TextView mAccountView, mUsernameView;
    private final ListView mSensitiveDataList, mSecurityQuestionsList;
    private final ImageButton mEditButton, mDeleteButton;

    private EntryDetailItemAdapter mSensitiveDataAdapter, mSecurityQuestionAdapter;
    private final VaultManager mVaultManager;
    private EntryRecord record;

    public EntryRecordViewHolder(Context context, ViewGroup parent) {
        super(LayoutInflater.from(context).inflate(R.layout.view_entry_record, parent, false));
        this.context = context;

        mDetailLayout = (LinearLayout) itemView.findViewById(R.id.entry_details);
        mAccountView = (TextView) itemView.findViewById(R.id.record_account);
        mUsernameView = (TextView) itemView.findViewById(R.id.record_username);
        mSensitiveDataList = (ListView) itemView.findViewById(R.id.record_sensitive_data_list);
        mSecurityQuestionsList = (ListView) itemView.findViewById(R.id.record_security_question_list);
        mEditButton = (ImageButton) itemView.findViewById(R.id.record_edit);
        mDeleteButton = (ImageButton) itemView.findViewById(R.id.record_delete);

        mDetailLayout.setVisibility(View.GONE);

        mVaultManager = AppVault.get().getManager();
    }

    public void bind(EntryRecord record) {
        this.record = record;

        mAccountView.setText(record.getAccount());
        mUsernameView.setText(record.getUsername());
    }

    public void setExpanded(boolean isExpanded) {
        if (record == null) return;
        //always ensure that any private info is cleared before losing reference to the adapters
        if (mSensitiveDataAdapter != null) mSensitiveDataAdapter.freeAllModels();
        if (mSecurityQuestionAdapter != null) mSecurityQuestionAdapter.freeAllModels();

        if (isExpanded) {
            mSensitiveDataAdapter = new EntryDetailItemAdapter<>(context, mVaultManager.getSensitiveEntries(record));
            mSensitiveDataList.setAdapter(mSensitiveDataAdapter);
            mSecurityQuestionAdapter = new EntryDetailItemAdapter<>(context, mVaultManager.getSecurityQuestions(record));
            mSecurityQuestionsList.setAdapter(mSecurityQuestionAdapter);

            mEditButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
            mDetailLayout.setVisibility(View.VISIBLE);
        } else {
            mEditButton.setVisibility(View.GONE);
            mDeleteButton.setVisibility(View.GONE);
            mDetailLayout.setVisibility(View.GONE);
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void setOnEditCLickListener(final EntryAdapter.OnActionListener listener) {
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onEditClicked(record);
            }
        });
    }

    public void setOnDeleteCLickListener(final EntryAdapter.OnActionListener listener) {
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onDeleteClicked(record);
            }
        });
    }

}
