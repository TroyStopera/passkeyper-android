package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.passkeyper.android.vaultmodel.VaultModel;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import java.util.Arrays;

/**
 * The helper class for the SQLite implementation of the local vault.
 */
class DbHelper extends SQLiteOpenHelper {

    private char[] password;
    private boolean isClosed = false;

    DbHelper(Context context, char[] password) {
        super(context, DbContract.DATABASE_NAME, null, DbContract.DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
        this.password = Arrays.copyOf(password, password.length);
    }

    @Override
    public void close() {
        Arrays.fill(password, '\0');
        isClosed = true;
        super.close();
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //init the encryption
        StringBuilder builder = new StringBuilder();
        builder.append("PRAGMA key = '");
        for (char c : password)
            builder.append(c);
        builder.append("';");
        db.execSQL(builder.toString());
        //build the tables
        db.execSQL(DbContract.RecordTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.SensitiveEntryTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.SecurityQuestionTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    Cursor readAllRecords() {
        return read(DbContract.RecordTable.TABLE_NAME, DbContract.RecordTable.COLUMNS, " 1 = 1", null);
    }

    Cursor readEntry(String table, String[] projection, String recordIdColumn, long recordId) {
        return read(table, projection, recordIdColumn + " = ?", new String[]{String.valueOf(recordId)});
    }

    void resetKey(char[] key) {
        if (isClosed())
            throw new IllegalStateException("Database accessed after being closed");

        SQLiteDatabase db = this.getWritableDatabase(password);

        StringBuilder builder = new StringBuilder();
        builder.append("PRAGMA rekey = '");
        for (char c : key)
            builder.append(c);
        builder.append("';");

        db.execSQL(builder.toString());

        //update the password field
        Arrays.fill(password, '\0');
        password = Arrays.copyOf(key, key.length);
    }

    long save(VaultModel model, String table, String _id, ContentValues values) {
        if (model.isSaved())
            save(table, values, _id + " = ?", new String[]{String.valueOf(model.getId())}, true);
        else
            return save(table, values, null, null, false);
        return model.getId();
    }

    void delete(VaultModel model, String table, String _id) {
        delete(table, _id + " = ?", new String[]{String.valueOf(model.getId())});
    }

    /*
        Below are the helper private methods for accessing the database
     */

    private Cursor read(String table, String[] projection, String selection, String[] args) {
        if (isClosed())
            throw new IllegalStateException("Database accessed after being closed");

        return this.getReadableDatabase(password).query(table, projection, selection, args, null, null, null);
    }

    private long save(String table, ContentValues values, String selection, String[] args, boolean update) {
        if (isClosed())
            throw new IllegalStateException("Database accessed after being closed");

        SQLiteDatabase db = this.getWritableDatabase(password);

        long id = -1;
        if (update) db.update(table, values, selection, args);
        else id = db.insert(table, null, values);

        db.close();
        return id;
    }

    private void delete(String table, String selection, String[] args) {
        if (isClosed())
            throw new IllegalStateException("Database accessed after being closed");

        SQLiteDatabase db = this.getWritableDatabase(password);
        db.delete(table, selection, args);
        db.close();
    }

}
