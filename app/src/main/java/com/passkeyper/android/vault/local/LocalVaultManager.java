package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.CharArrayBuffer;
import android.database.Cursor;

import com.passkeyper.android.vault.VaultManager;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Vault manager that handles reading and updating the local vault database.
 */
public class LocalVaultManager extends VaultManager {

    private char[] password;
    private final DbHelper dbHelper;

    /**
     * Create a new VaultManager with the given password to access the local vault.
     *
     * @param context  the Context used to access the database.
     * @param password the password to unlock the database.
     * @throws DatabaseAuthException when an incorrect password is provided
     */
    public LocalVaultManager(Context context, char[] password) throws DatabaseAuthException {
        if (isLocalDbSetup(context)) {
            dbHelper = new DbHelper(context);
            this.password = Arrays.copyOf(password, password.length);
        } else
            throw new IllegalStateException("Cannot access LocalVault before setting up the Local Db");

        //verify the password to be correct
        if (!dbHelper.isKeyValid(password))
            throw new DatabaseAuthException("Invalid password provided");
    }

    public void changePassword(char[] newPassword) {
        dbHelper.updateKey(password, newPassword);

        Arrays.fill(password, '\0');
        password = Arrays.copyOf(newPassword, newPassword.length);
    }

    @Override
    public void close() {
        super.close();
        dbHelper.close();
        Arrays.fill(password, '\0');
    }

    @Override
    public List<EntryRecord> getAllEntryRecords() {
        List<EntryRecord> records = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readAllRecords(password);

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
        cursor.close();

        return records;
    }

    @Override
    public List<SensitiveEntry> getSensitiveEntries(EntryRecord record) {
        List<SensitiveEntry> entries = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readEntry(
                password,
                DbContract.SensitiveEntryTable.TABLE_NAME,
                DbContract.SensitiveEntryTable.COLUMNS,
                DbContract.SensitiveEntryTable.COLUMN_NAME_RECORD_ID,
                record.getId());

        if (cursor.moveToFirst()) do {
            SensitiveEntry entry = new SensitiveEntry(record);
            //read string data
            entry.setName(cursor.getString(1));
            //read the private data
            CharArrayBuffer buffer = new CharArrayBuffer(16);
            cursor.copyStringToBuffer(2, buffer);
            entry.setValue(Arrays.copyOfRange(buffer.data, 0, buffer.sizeCopied));
            //clear the private data
            Arrays.fill(buffer.data, '\0');
            //update id
            setModelID(entry, cursor.getLong(0));
            //add to list
            entries.add(entry);
        } while (cursor.moveToNext());
        cursor.close();

        return entries;
    }

    @Override
    public List<SecurityQuesEntry> getSecurityQuestions(EntryRecord record) {
        List<SecurityQuesEntry> entries = new LinkedList<>();
        //query db
        Cursor cursor = dbHelper.readEntry(
                password,
                DbContract.SecurityQuestionTable.TABLE_NAME,
                DbContract.SecurityQuestionTable.COLUMNS,
                DbContract.SecurityQuestionTable.COLUMN_NAME_RECORD_ID,
                record.getId());

        if (cursor.moveToFirst()) do {
            SecurityQuesEntry entry = new SecurityQuesEntry(record);
            //read string data
            entry.setQuestion(cursor.getString(1));
            //read the private data
            CharArrayBuffer buffer = new CharArrayBuffer(16);
            cursor.copyStringToBuffer(2, buffer);
            entry.setAnswer(Arrays.copyOfRange(buffer.data, 0, buffer.sizeCopied));
            //clear the private data
            Arrays.fill(buffer.data, '\0');
            //update id
            setModelID(entry, cursor.getLong(0));
            //add to list
            entries.add(entry);
        } while (cursor.moveToNext());
        cursor.close();

        return entries;
    }

    @Override
    public void save(EntryRecord record) {
        ContentValues values = new ContentValues();
        values.put(DbContract.RecordTable.COLUMN_NAME_ACCOUNT, record.getAccount());
        values.put(DbContract.RecordTable.COLUMN_NAME_USERNAME, record.getUsername());

        setModelID(record,
                dbHelper.save(
                        password,
                        record,
                        DbContract.RecordTable.TABLE_NAME,
                        values
                )
        );
    }

    @Override
    public void save(SensitiveEntry sensitiveEntry) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_NAME, sensitiveEntry.getName());
        values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_VALUE, new String(sensitiveEntry.getValue()));
        values.put(DbContract.SensitiveEntryTable.COLUMN_NAME_RECORD_ID, sensitiveEntry.getRecord().getId());

        setModelID(sensitiveEntry,
                dbHelper.save(
                        password,
                        sensitiveEntry,
                        DbContract.SensitiveEntryTable.TABLE_NAME,
                        values
                )
        );
    }

    @Override
    public void save(SecurityQuesEntry securityQuesEntry) {
        ContentValues values = new ContentValues();
        values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_QUESTION, securityQuesEntry.getQuestion());
        values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_ANSWER, new String(securityQuesEntry.getAnswer()));
        values.put(DbContract.SecurityQuestionTable.COLUMN_NAME_RECORD_ID, securityQuesEntry.getRecord().getId());

        setModelID(securityQuesEntry,
                dbHelper.save(
                        password,
                        securityQuesEntry,
                        DbContract.SecurityQuestionTable.TABLE_NAME,
                        values
                )
        );
    }

    @Override
    public void delete(EntryRecord record) {
        //delete all associated models as well
        for (SensitiveEntry entry : getSensitiveEntries(record))
            delete(entry);
        for (SecurityQuesEntry entry : getSecurityQuestions(record))
            delete(entry);

        dbHelper.delete(
                password,
                record,
                DbContract.RecordTable.TABLE_NAME
        );
    }

    @Override
    public void delete(SensitiveEntry sensitiveEntry) {
        dbHelper.delete(
                password,
                sensitiveEntry,
                DbContract.SensitiveEntryTable.TABLE_NAME
        );
    }

    @Override
    public void delete(SecurityQuesEntry securityQuesEntry) {
        dbHelper.delete(
                password,
                securityQuesEntry,
                DbContract.SecurityQuestionTable.TABLE_NAME
        );
    }

    /**
     * Used to determine if the local database has been created and setup.
     *
     * @param context a Context used to load the database.
     * @return true if the database has been setup.
     */
    public static boolean isLocalDbSetup(Context context) {
        return new DbHelper(context).isKeySet();
    }

    /**
     * Sets up the local database to use the given password.
     *
     * @param context a Context used to load the database.
     * @param key     the key that is used to encrypt the database.
     */
    public static void setupLocalDb(Context context, char[] key) {
        if (isLocalDbSetup(context))
            throw new IllegalStateException("Cannot setup the local database twice");
        new DbHelper(context).updateKey(DbHelper.defaultKey(), key);
    }

}
