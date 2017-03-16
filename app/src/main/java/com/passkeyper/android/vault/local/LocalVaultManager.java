package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.passkeyper.android.crypto.LocalDbCrypto;
import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;

import java.util.LinkedList;
import java.util.List;

/**
 * Vault manager that handles reading and updating the local vault database.
 */
public class LocalVaultManager extends VaultManager {

    private final DbHelper dbHelper;
    /* Used when an exception is thrown during encryption/decryption to avoid leaking sensitive data */
    private static final String ERROR_STRING = "*Error during encryption/decryption*";

    /**
     * Create a new DbHelper to access the local vault.
     *
     * @param context the Context used to access the database.
     */
    public LocalVaultManager(Context context) {
        dbHelper = new DbHelper(context);
    }

    @Override
    public List<EntryRecord> getAllEntryRecords() {
        List<EntryRecord> records = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readAllRecords();

        if (cursor.moveToFirst()) do {
            EntryRecord record = new EntryRecord();
            //read data
            record.setAccount(cursor.getString(1));
            record.setUsername(cursor.getString(2));
            //update id
            setModelID(record, cursor.getLong(0));
            //add to list
            records.add(record);
        } while (cursor.moveToNext());

        return records;
    }

    @Override
    public List<SensitiveEntry> getSensitiveEntries(EntryRecord record) {
        List<SensitiveEntry> entries = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readEntry(
                DbContract.SensitiveEntryTable.TABLE_NAME,
                DbContract.SensitiveEntryTable.COLUMNS,
                DbContract.SensitiveEntryTable.COLUMN_NAME_RECORD_ID,
                record.getId());

        if (cursor.moveToFirst()) do {
            SensitiveEntry entry = new SensitiveEntry(record);
            //read data
            entry.setName(cursor.getString(1));
            //try to decrypt encrypted data
            try {
                entry.setValue(LocalDbCrypto.decrypt(cursor.getString(2)));
            } catch (Exception e) {
                //fall back to error string
                entry.setValue(ERROR_STRING.toCharArray());
            }
            //update id
            setModelID(entry, cursor.getLong(0));
            //add to list
            entries.add(entry);
        } while (cursor.moveToNext());

        return entries;
    }

    @Override
    public List<SecurityQuesEntry> getSecurityQuestions(EntryRecord record) {
        List<SecurityQuesEntry> entries = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readEntry(
                DbContract.SecurityQuestionTable.TABLE_NAME,
                DbContract.SecurityQuestionTable.COLUMNS,
                DbContract.SecurityQuestionTable.COLUMN_NAME_RECORD_ID,
                record.getId());

        if (cursor.moveToFirst()) do {
            SecurityQuesEntry entry = new SecurityQuesEntry(record);
            //read data
            entry.setQuestion(cursor.getString(1));
            //try to decrypt encrypted data
            try {
                entry.setAnswer(LocalDbCrypto.decrypt(cursor.getString(2)));
            } catch (Exception e) {
                //fall back to error string
                entry.setAnswer(ERROR_STRING.toCharArray());
            }
            //update id
            setModelID(entry, cursor.getLong(0));
            //add to list
            entries.add(entry);
        } while (cursor.moveToNext());

        return entries;
    }

    @Override
    public void save(EntryRecord record) {
        ContentValues values = new ContentValues();
        values.put(DbContract.RecordTable.COLUMN_NAME_ACCOUNT, record.getAccount());
        values.put(DbContract.RecordTable.COLUMN_NAME_USERNAME, record.getUsername());

        dbHelper.save(record, DbContract.RecordTable.TABLE_NAME, DbContract.RecordTable._ID, values);
    }

    @Override
    public void save(SensitiveEntry sensitiveEntry) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_NAME, sensitiveEntry.getName());
        values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_RECORD_ID, sensitiveEntry.getRecord().getId());
        //try to encrypt sensitive data
        try {
            values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_VALUE, LocalDbCrypto.encrypt(sensitiveEntry.getValue()));
        } catch (Exception e) {
            //fall back to saving the error string
            values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_VALUE, ERROR_STRING);
        }

        dbHelper.save(sensitiveEntry, DbContract.SensitiveEntryTable.TABLE_NAME, DbContract.SensitiveEntryTable._ID, values);
    }

    @Override
    public void save(SecurityQuesEntry securityQuesEntry) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_QUESTION, securityQuesEntry.getQuestion());
        values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_RECORD_ID, securityQuesEntry.getRecord().getId());
        //try to encrypt sensitive data
        try {
            values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_ANSWER, LocalDbCrypto.encrypt(securityQuesEntry.getAnswer()));
        } catch (Exception e) {
            //fall back to saving the error string
            values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_ANSWER, ERROR_STRING);
        }

        dbHelper.save(securityQuesEntry, DbContract.SecurityQuestionTable.TABLE_NAME, DbContract.SecurityQuestionTable._ID, values);
    }

    @Override
    public void delete(EntryRecord record) {
        dbHelper.delete(record, DbContract.RecordTable.TABLE_NAME, DbContract.RecordTable._ID);
    }

    @Override
    public void delete(SensitiveEntry sensitiveEntry) {
        dbHelper.delete(sensitiveEntry, DbContract.SensitiveEntryTable.TABLE_NAME, DbContract.SensitiveEntryTable._ID);
    }

    @Override
    public void delete(SecurityQuesEntry securityQuesEntry) {
        dbHelper.delete(securityQuesEntry, DbContract.SecurityQuestionTable.TABLE_NAME, DbContract.SecurityQuestionTable._ID);
    }

}
