package com.passkeyper.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import com.passkeyper.android.R;
import com.passkeyper.android.Vault;
import com.passkeyper.android.adapter.SecurityQuesAdapter;
import com.passkeyper.android.adapter.SensitiveEntryAdapter;
import com.passkeyper.android.util.SnackbarUndoDelete;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.PrivateModel;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.vaultmodel.VaultModel;
import com.passkeyper.android.view.PrivateVaultModelEditView;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class EditEntryActivity extends AppCompatActivity implements PrivateVaultModelEditView.OnDeletePressedListener, View.OnClickListener, SnackbarUndoDelete.SnackBarDeleteListener<PrivateModel> {

    public static final int RESULT_ENTRY_DELETED = 100;
    public static final int RESULT_ENTRY_CREATED = 200;
    public static final int RESULT_ENTRY_UPDATED = 300;
    public static final String ENTRY_RECORD_EXTRA_KEY = "EntryRecord";

    private TextInputLayout accountInputLayout;
    private TextInputEditText accountInput, usernameInput;
    private ListView sensitiveList, securityList;
    private SensitiveEntryAdapter sensitiveEntryAdapter;
    private SecurityQuesAdapter securityQuesAdapter;
    private SnackbarUndoDelete<PrivateModel> snackbarUndoDelete;

    private EntryRecord record;
    /* the list of all models that should be deleted if the user saves their edits */
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
                //clear focus of any EditText that is being edited
                View focus = getCurrentFocus();
                if (focus != null) focus.clearFocus();
                save();
                return true;
            case R.id.action_delete:
                //verify deleting
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.edit_entry_popup_title);
                builder.setMessage(R.string.edit_entry_popup_msg);
                builder.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    delete();
                    dialogInterface.dismiss();
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
        PrivateModel privateModel = view.getVaultModel();
        if (privateModel instanceof SensitiveEntry)
            sensitiveEntryAdapter.remove((SensitiveEntry) privateModel);
        else if (privateModel instanceof SecurityQuesEntry)
            securityQuesAdapter.remove((SecurityQuesEntry) privateModel);
        snackbarUndoDelete.addUndoable(privateModel);
    }

    @Override
    public void onClick(View view) {
        //when a view is clicked, clear focus to force writing VaultModelEditViews values to model
        View focus = getCurrentFocus();
        if (focus != null) focus.clearFocus();

        switch (view.getId()) {
            case R.id.press_sensitive:
            case R.id.edit_add_sensitive:
                sensitiveEntryAdapter.addVaultModel(new SensitiveEntry(record));
                sensitiveList.setSelection(sensitiveList.getAdapter().getCount() - 1);
                break;
            case R.id.press_security:
            case R.id.edit_add_security:
                securityQuesAdapter.addVaultModel(new SecurityQuesEntry(record));
                securityList.setSelection(securityList.getAdapter().getCount() - 1);
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
    public void onDelete(Collection<PrivateModel> privateModels) {
        deletedModels.addAll(privateModels);
    }

    @Override
    public void onUndo(Collection<PrivateModel> privateModels) {
        for (PrivateModel privateModel : privateModels) {
            if (privateModel instanceof SensitiveEntry)
                sensitiveEntryAdapter.addVaultModel((SensitiveEntry) privateModel);
            else if (privateModel instanceof SecurityQuesEntry)
                securityQuesAdapter.addVaultModel((SecurityQuesEntry) privateModel);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_entry);

        snackbarUndoDelete = new SnackbarUndoDelete<>(
                findViewById(R.id.edit_activity_root),
                getString(R.string.edit_entry_item_deleted),
                getString(R.string.edit_entry_items_deleted),
                this
        );

        if (getIntent().hasExtra(ENTRY_RECORD_EXTRA_KEY))
            record = getIntent().getParcelableExtra(ENTRY_RECORD_EXTRA_KEY);
        else record = new EntryRecord();

        accountInputLayout = (TextInputLayout) findViewById(R.id.input_layout_account);
        accountInput = (TextInputEditText) findViewById(R.id.input_account);
        if (!record.getAccount().isEmpty()) accountInput.setText(record.getAccount());
        usernameInput = (TextInputEditText) findViewById(R.id.input_username);
        if (!record.getUsername().isEmpty()) usernameInput.setText(record.getUsername());

        //setup the sensitiveList entry list
        sensitiveList = (ListView) findViewById(R.id.edit_sensitive_list);
        sensitiveEntryAdapter = new SensitiveEntryAdapter(this);
        sensitiveEntryAdapter.setOnDeletePressedListener(this);
        sensitiveList.setEmptyView(findViewById(R.id.edit_empty_list_sensitive));
        sensitiveList.setAdapter(sensitiveEntryAdapter);

        //setup the securityList question list
        securityList = (ListView) findViewById(R.id.edit_security_list);
        securityQuesAdapter = new SecurityQuesAdapter(this);
        securityQuesAdapter.setOnDeletePressedListener(this);
        securityList.setEmptyView(findViewById(R.id.edit_empty_list_security));
        securityList.setAdapter(securityQuesAdapter);

        //if its a new record then add blank entries
        if (!record.isSaved()) {
            sensitiveEntryAdapter.addVaultModel(new SensitiveEntry(record));
            securityQuesAdapter.addVaultModel(new SecurityQuesEntry(record));
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

    @Override
    protected void onResume() {
        super.onResume();
        Vault vault = Vault.get();

        if (vault.hasManager() || vault.loadManager()) {
            if (record.isSaved()) {
                VaultManager vaultManager = vault.getManager();
                //add any new vault models
                sensitiveEntryAdapter.addNewVaultModels(vaultManager.getSensitiveEntries(record));
                securityQuesAdapter.addNewVaultModels(vaultManager.getSecurityQuestions(record));
            }
        } else vault.requestSignIn(this);
    }

    private boolean verifyInput() {
        boolean valid = true;

        //verify that the account is defined
        if (accountInput.length() <= 0) {
            accountInputLayout.setError(getString(R.string.error_field_required));
            valid = false;
        } else accountInputLayout.setErrorEnabled(false);

        //remove any sensitive entries not filled out
        for (SensitiveEntry entry : sensitiveEntryAdapter.getAllVaultModels()) {
            if (!entry.hasValue()) {
                sensitiveEntryAdapter.remove(entry);
                deletedModels.add(entry);
            }
        }

        //verify the security question - they all need a question and answer
        SecurityQuesEntry[] entries = securityQuesAdapter.getAllVaultModels();
        for (int i = 0; i < entries.length; i++) {
            SecurityQuesEntry entry = entries[i];
            //remove totally empty entries
            if (!entry.hasQuestion() && !entry.hasAnswer()) {
                securityQuesAdapter.remove(entry);
                deletedModels.add(entry);
            }
            //handle entries that are incomplete
            else if (!entry.hasQuestion() || !entry.hasAnswer()) {
                securityQuesAdapter.setVerifyModeEnabled(true);
                securityList.setSelection(i);
                valid = false;
            }
        }

        if (valid) securityQuesAdapter.setVerifyModeEnabled(false);

        return valid;
    }

    private void save() {
        boolean created = !record.isSaved();
        if (!verifyInput()) return;

        //delete any pending deletions by closing the Snackbar
        snackbarUndoDelete.forceDismissSnackbar();

        VaultManager vaultManager = Vault.get().getManager();

        for (VaultModel model : deletedModels) {
            vaultManager.delete(model);
            //free from memory when needed
            if (model instanceof PrivateModel)
                ((PrivateModel) model).free();
        }

        record.setAccount(accountInput.getText().toString());
        record.setUsername(usernameInput.getText().toString());
        vaultManager.save(record);

        for (SensitiveEntry entry : sensitiveEntryAdapter.getAllVaultModels()) {
            //only save if the user has filled out the field
            if (entry.hasValue() && entry.hasName()) {
                vaultManager.save(entry);
            }
            entry.free();
        }

        for (SecurityQuesEntry entry : securityQuesAdapter.getAllVaultModels()) {
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
        VaultManager vaultManager = Vault.get().getManager();
        for (VaultModel model : deletedModels) {
            vaultManager.delete(model);
            //free from memory when needed
            if (model instanceof SensitiveEntry)
                ((SensitiveEntry) model).free();
            else if (model instanceof SecurityQuesEntry)
                ((SecurityQuesEntry) model).free();
        }

        vaultManager.delete(record);

        for (SensitiveEntry entry : sensitiveEntryAdapter.getAllVaultModels()) {
            vaultManager.delete(entry);
            entry.free();
        }

        for (SecurityQuesEntry entry : securityQuesAdapter.getAllVaultModels()) {
            vaultManager.delete(entry);
            entry.free();
        }

        Intent data = new Intent();
        data.putExtra(ENTRY_RECORD_EXTRA_KEY, record);
        setResult(RESULT_ENTRY_DELETED, data);
        finish();
    }

}
