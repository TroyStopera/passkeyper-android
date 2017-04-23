package com.passkeyper.android.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.passkeyper.android.R;
import com.passkeyper.android.vaultmodel.PrivateModel;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;

/**
 * View used to display an EntryRecord's sub-items.
 */
public class EntryRecordDetailItemView extends FrameLayout {

    private final TextView title, value;

    public EntryRecordDetailItemView(@NonNull Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.view_entry_record_detail_item, this);

        title = (TextView) findViewById(R.id.record_detail_item_title);
        value = (TextView) findViewById(R.id.record_detail_item_value);
    }

    public void setModel(PrivateModel model) {
        if (model instanceof SensitiveEntry) {
            SensitiveEntry entry = (SensitiveEntry) model;
            char[] valueChars = entry.getValue();
            title.setText(entry.getName());
            value.setText(valueChars, 0, valueChars.length);
        } else if (model instanceof SecurityQuesEntry) {
            SecurityQuesEntry entry = (SecurityQuesEntry) model;
            char[] quesChars = entry.getAnswer();
            title.setText(entry.getQuestion());
            value.setText(quesChars, 0, quesChars.length);
        }
    }

}
