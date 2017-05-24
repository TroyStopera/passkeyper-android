package com.passkeyper.android.vault.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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
        db.execSQL(DbContract.SecurityQuestionTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.RecordTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.SensitiveEntryTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.RecoveryTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    void updateKey(char[] oldKey, char[] newKey) {
        SQLiteDatabase db = getWritableDatabase(oldKey);

        StringBuilder builder = new StringBuilder();
        builder.append("PRAGMA rekey = '");
        for (char c : newKey)
            builder.append(c);
        builder.append("';");

        db.execSQL(builder.toString());
        db.close();
    }

    Cursor read(char[] key, String table, String[] projection, String selection, String[] args) {
        return getReadableDatabase(key).query(table, projection, selection, args, null, null, null);
    }

    long save(char[] key, String table, ContentValues values, String selection, String[] args, boolean update) {
        SQLiteDatabase db = this.getWritableDatabase(key);

        long id = -1;
        if (update) db.update(table, values, selection, args);
        else id = db.insert(table, null, values);

        db.close();
        return id;
    }

    void delete(char[] key, String table, String selection, String[] args) {
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
