package com.passkeyper.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.passkeyper.android.adapter.SecurityQuesAdapter;
import com.passkeyper.android.adapter.SensitiveEntryAdapter;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.vaultmodel.VaultModel;
import com.passkeyper.android.view.PrivateVaultModelEditView;
import com.passkeyper.android.view.SecurityQuestionEditView;
import com.passkeyper.android.view.SensitiveEntryEditView;

import java.util.LinkedList;
import java.util.List;

public class EditEntry extends AppCompatActivity implements PrivateVaultModelEditView.OnDeletePressedListener, View.OnClickListener {

    public static final int RESULT_ENTRY_DELETED = 0;
    public static final int RESULT_ENTRY_CREATED = 1;
    public static final int RESULT_ENTRY_UPDATED = 2;
    public static final String ENTRY_RECORD_EXTRA_KEY = "EntryRecord";

    private TextInputLayout mAccountInputLayout;
    private TextInputEditText mAccountInput, mUsernameInput;
    private ListView mSensitiveList, mSecurityList;
    private SensitiveEntryAdapter mSensitiveEntryAdapter;
    private SecurityQuesAdapter mSecurityQuesAdapter;
    private Snackbar mSnackbar;

    private EntryRecord record;
    private VaultManager vaultManager;

    private final List<VaultModel> deletedModels = new LinkedList<>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_entry, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                return true;
            case R.id.action_delete:
                //verify deleting
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.edit_entry_popup_title);
                builder.setMessage(R.string.edit_entry_popup_msg);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete();
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDeletePressed(PrivateVaultModelEditView view) {
        int removedIndex = -1;
        if (view instanceof SensitiveEntryEditView)
            removedIndex = mSensitiveEntryAdapter.remove(((SensitiveEntryEditView) view).getVaultModel());
        else if (view instanceof SecurityQuestionEditView)
            removedIndex = mSecurityQuesAdapter.remove(((SecurityQuestionEditView) view).getVaultModel());

        showUndo(removedIndex, view.getVaultModel());
    }

    @Override
    public void onClick(View view) {
        //when a view is clicked clear focus to force writing VaultModelEditViews values to model
        View focus = getCurrentFocus();
        if (focus != null) focus.clearFocus();

        switch (view.getId()) {
            case R.id.press_sensitive:
            case R.id.edit_add_sensitive:
                mSensitiveEntryAdapter.addVaultModel(new SensitiveEntry(record));
                mSensitiveList.setSelection(mSensitiveList.getAdapter().getCount() - 1);
                break;
            case R.id.press_security:
            case R.id.edit_add_security:
                mSecurityQuesAdapter.addVaultModel(new SecurityQuesEntry(record));
                mSecurityList.setSelection(mSecurityList.getAdapter().getCount() - 1);
                break;
        }

        //hide the keyboard
        focus = getCurrentFocus();
        if (focus != null) {
            focus.clearFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        Intent intent = getIntent();
        if (intent.hasExtra(ENTRY_RECORD_EXTRA_KEY))
            record = intent.getParcelableExtra(ENTRY_RECORD_EXTRA_KEY);
        else record = new EntryRecord();

        vaultManager = VaultManager.get(this);

        mAccountInputLayout = (TextInputLayout) findViewById(R.id.input_layout_account);
        mAccountInput = (TextInputEditText) findViewById(R.id.input_account);
        if (!record.getAccount().isEmpty()) mAccountInput.setText(record.getAccount());
        mUsernameInput = (TextInputEditText) findViewById(R.id.input_username);
        if (!record.getUsername().isEmpty()) mUsernameInput.setText(record.getUsername());

        //setup the mSensitiveList entry list
        mSensitiveList = (ListView) findViewById(R.id.edit_sensitive_list);
        mSensitiveEntryAdapter = new SensitiveEntryAdapter(this);
        mSensitiveEntryAdapter.setOnDeletePressedListener(this);
        mSensitiveList.setEmptyView(findViewById(R.id.edit_empty_list_sensitive));
        mSensitiveList.setAdapter(mSensitiveEntryAdapter);

        //setup the mSecurityList question list
        mSecurityList = (ListView) findViewById(R.id.edit_security_list);
        mSecurityQuesAdapter = new SecurityQuesAdapter(this);
        mSecurityQuesAdapter.setOnDeletePressedListener(this);
        mSecurityList.setEmptyView(findViewById(R.id.edit_empty_list_security));
        mSecurityList.setAdapter(mSecurityQuesAdapter);

        //populate the lists
        if (record.isSaved()) {
            mSensitiveEntryAdapter.addVaultModels(vaultManager.getSensitiveEntries(record));
            mSecurityQuesAdapter.addVaultModels(vaultManager.getSecurityQuestions(record));
        } else {
            mSensitiveEntryAdapter.addVaultModel(new SensitiveEntry(record));
            mSecurityQuesAdapter.addVaultModel(new SecurityQuesEntry(record));
        }

        //listen for adding entries
        findViewById(R.id.edit_add_sensitive).setOnClickListener(this);
        findViewById(R.id.press_sensitive).setOnClickListener(this);
        findViewById(R.id.edit_add_security).setOnClickListener(this);
        findViewById(R.id.press_security).setOnClickListener(this);

        // enable the back button
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showUndo(final int index, final VaultModel model) {
        mSnackbar = Snackbar.make(
                findViewById(R.id.edit_activity_root),
                R.string.edit_entry_item_deleted,
                Snackbar.LENGTH_LONG
        );

        final BaseTransientBottomBar.BaseCallback<Snackbar> callback = new BaseTransientBottomBar.BaseCallback<Snackbar>() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                deletedModels.add(model);
            }
        };

        mSnackbar.setAction(R.string.action_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model instanceof SensitiveEntry)
                    mSensitiveEntryAdapter.addVaultModel(index, (SensitiveEntry) model);
                else if (model instanceof SecurityQuesEntry)
                    mSecurityQuesAdapter.addVaultModel(index, (SecurityQuesEntry) model);

                //no need to add vault model to deleted when undone
                mSnackbar.removeCallback(callback);
            }
        });

        mSnackbar.addCallback(callback);

        //hide keyboard on delete so "undo" can be seen
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(findViewById(R.id.edit_activity_root).getWindowToken(), 0);

        mSnackbar.show();
    }

    private boolean verifyInput() {
        boolean valid = true;

        //verify that the account is defined
        if (mAccountInput.length() <= 0) {
            mAccountInputLayout.setError(getString(R.string.error_field_required));
            valid = false;
        } else mAccountInputLayout.setErrorEnabled(false);

        //remove any sensitive entries not filled out
        for (SensitiveEntry entry : mSensitiveEntryAdapter.getAllVaultModels()) {
            if (!entry.hasValue()) {
                mSensitiveEntryAdapter.remove(entry);
                deletedModels.add(entry);
            }
        }

        //verify the security question - they all need a question and answer
        SecurityQuesEntry[] entries = mSecurityQuesAdapter.getAllVaultModels();
        for (int i = 0; i < entries.length; i++) {
            SecurityQuesEntry entry = entries[i];
            //remove totally empty entries
            if (!entry.hasQuestion() && !entry.hasAnswer()) {
                mSecurityQuesAdapter.remove(entry);
                deletedModels.add(entry);
            }
            //handle entries that are incomplete
            else if (!entry.hasQuestion() || !entry.hasAnswer()) {
                mSecurityQuesAdapter.setVerifyModeEnabled(true);
                mSecurityList.setSelection(i);
                valid = false;
            }
        }

        if (valid) mSecurityQuesAdapter.setVerifyModeEnabled(false);

        return valid;
    }

    private void save() {
        boolean created = !record.isSaved();
        if (!verifyInput()) return;

        for (VaultModel model : deletedModels) {
            vaultManager.delete(model);
            //free from memory when needed
            if (model instanceof SensitiveEntry)
                ((SensitiveEntry) model).free();
            else if (model instanceof SecurityQuesEntry)
                ((SecurityQuesEntry) model).free();
        }

        record.setAccount(mAccountInput.getText().toString());
        record.setUsername(mUsernameInput.getText().toString());
        vaultManager.save(record);

        for (SensitiveEntry entry : mSensitiveEntryAdapter.getAllVaultModels()) {
            //only save if the user has filled out the field
            if (entry.hasValue() && entry.hasName()) {
                vaultManager.save(entry);
            }
            entry.free();
        }

        for (SecurityQuesEntry entry : mSecurityQuesAdapter.getAllVaultModels()) {
            vaultManager.save(entry);
            entry.free();
        }

        if (created) {
            Intent data = new Intent();
            data.putExtra(ENTRY_RECORD_EXTRA_KEY, record);
            setResult(RESULT_ENTRY_CREATED, data);
        } else setResult(RESULT_ENTRY_UPDATED);

        finish();
    }

    private void delete() {
        for (VaultModel model : deletedModels) {
            vaultManager.delete(model);
            //free from memory when needed
            if (model instanceof SensitiveEntry)
                ((SensitiveEntry) model).free();
            else if (model instanceof SecurityQuesEntry)
                ((SecurityQuesEntry) model).free();
        }

        vaultManager.delete(record);

        for (SensitiveEntry entry : mSensitiveEntryAdapter.getAllVaultModels()) {
            vaultManager.delete(entry);
            entry.free();
        }

        for (SecurityQuesEntry entry : mSecurityQuesAdapter.getAllVaultModels()) {
            vaultManager.delete(entry);
            entry.free();
        }

        Intent data = new Intent();
        data.putExtra(ENTRY_RECORD_EXTRA_KEY, record);
        setResult(RESULT_ENTRY_DELETED, data);
        finish();
    }

}
