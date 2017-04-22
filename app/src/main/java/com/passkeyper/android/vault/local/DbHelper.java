package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.passkeyper.android.vaultmodel.VaultModel;

/**
 * The helper class for the SQLite implementation of the local vault.
 */
class DbHelper extends SQLiteOpenHelper {

    DbHelper(Context context) {
        super(context, DbContract.DATABASE_NAME, null, DbContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(table, projection, selection, args, null, null, null);
    }

    private long save(String table, ContentValues values, String selection, String[] args, boolean update) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (update) db.update(table, values, selection, args);
        else return db.insert(table, null, values);
        return -1;
    }

    private void delete(String table, String selection, String[] args) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(table, selection, args);
    }

}
