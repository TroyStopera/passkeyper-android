package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.passkeyper.android.vaultmodel.VaultModel;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

/**
 * The helper class for the SQLite implementation of the local vault.
 */
class DbHelper extends SQLiteOpenHelper {

    DbHelper(Context context) {
        super(context, DbContract.DATABASE_NAME, null, DbContract.DATABASE_VERSION);
        SQLiteDatabase.loadLibs(context);
    }

    boolean isKeyValid(char[] key) {
        try {
            //if the database can be opened with the key then the key is valid
            getReadableDatabase(key).close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    boolean isKeySet() {
        //if the default key is valid then the key has not been set
        return !isKeyValid(defaultKey());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //set default key on create
        db.execSQL("PRAGMA key = 'p';");
        //build the tables
        db.execSQL(DbContract.RecordTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.SensitiveEntryTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.SecurityQuestionTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    Cursor readAllRecords(char[] key) {
        return read(key, DbContract.RecordTable.TABLE_NAME, DbContract.RecordTable.COLUMNS, " 1 = 1", null);
    }

    Cursor readEntry(char[] key, String table, String[] projection, String recordIdColumn, long recordId) {
        return read(key, table, projection, recordIdColumn + " = ?", new String[]{String.valueOf(recordId)});
    }

    void updateKey(char[] oldKey, char[] newKey) {
        SQLiteDatabase db = this.getWritableDatabase(oldKey);

        StringBuilder builder = new StringBuilder();
        builder.append("PRAGMA rekey = '");
        for (char c : newKey)
            builder.append(c);
        builder.append("';");

        db.execSQL(builder.toString());
    }

    long save(char[] key, VaultModel model, String table, String _id, ContentValues values) {
        if (model.isSaved())
            save(key, table, values, _id + " = ?", new String[]{String.valueOf(model.getId())}, true);
        else
            return save(key, table, values, null, null, false);
        return model.getId();
    }

    void delete(char[] key, VaultModel model, String table, String _id) {
        delete(key, table, _id + " = ?", new String[]{String.valueOf(model.getId())});
    }

    /*
        Below are the helper private methods for accessing the database
     */

    private Cursor read(char[] key, String table, String[] projection, String selection, String[] args) {
        return this.getReadableDatabase(key).query(table, projection, selection, args, null, null, null);
    }

    private long save(char[] key, String table, ContentValues values, String selection, String[] args, boolean update) {
        SQLiteDatabase db = this.getWritableDatabase(key);

        long id = -1;
        if (update) db.update(table, values, selection, args);
        else id = db.insert(table, null, values);

        db.close();
        return id;
    }

    private void delete(char[] key, String table, String selection, String[] args) {
        SQLiteDatabase db = this.getWritableDatabase(key);
        db.delete(table, selection, args);
        db.close();
    }

    /**
     * @return the default key used to encrypt the database.
     */
    static char[] defaultKey() {
        return new char[]{'p'};
    }

}
