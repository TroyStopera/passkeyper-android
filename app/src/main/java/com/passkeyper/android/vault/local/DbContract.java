package com.passkeyper.android.vault.local;

import android.provider.BaseColumns;

/**
 * Defines constants and SQL statements needed for the Local Vault Database.
 */
class DbContract {

    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "local_vault.db";

    static abstract class RecoveryTable implements BaseColumns {
        static final String TABLE_NAME = "recovery";
        static final String COLUMN_NAME_SECURITY_QUESTION = "question";
        static final String COLUMN_NAME_SECURITY_ANSWER = "answer";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_SECURITY_QUESTION + " TEXT NOT NULL, " +
                COLUMN_NAME_SECURITY_ANSWER + " TEXT NOT NULL);";

        static final String[] COLUMNS = {_ID, COLUMN_NAME_SECURITY_QUESTION, COLUMN_NAME_SECURITY_ANSWER};
    }

    static abstract class RecordTable implements BaseColumns {
        static final String TABLE_NAME = "record";
        static final String COLUMN_NAME_ACCOUNT = "account";
        static final String COLUMN_NAME_USERNAME = "username";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_ACCOUNT + " TEXT NOT NULL, " +
                COLUMN_NAME_USERNAME + " TEXT);";

        static final String[] COLUMNS = {_ID, COLUMN_NAME_ACCOUNT, COLUMN_NAME_USERNAME};
    }

    static abstract class SensitiveEntryTable implements BaseColumns {
        static final String TABLE_NAME = "sensitiveEntry";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_VALUE = "value";
        static final String COLUMN_NAME_RECORD_ID = "recordId";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_NAME + " TEXT NOT NULL, " +
                COLUMN_NAME_VALUE + " TEXT NOT NULL, " +
                COLUMN_NAME_RECORD_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_NAME_RECORD_ID + ") REFERENCES " +
                RecordTable.TABLE_NAME + "(" + RecordTable._ID + "));";

        static final String[] COLUMNS = {_ID, COLUMN_NAME_NAME, COLUMN_NAME_VALUE, COLUMN_NAME_RECORD_ID};
    }

    static abstract class SecurityQuestionTable implements BaseColumns {
        static final String TABLE_NAME = "securityQuestionEntry";
        static final String COLUMN_NAME_QUESTION = "question";
        static final String COLUMN_NAME_ANSWER = "answer";
        static final String COLUMN_NAME_RECORD_ID = "recordId";

        static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME_QUESTION + " TEXT NOT NULL, " +
                COLUMN_NAME_ANSWER + " TEXT NOT NULL, " +
                COLUMN_NAME_RECORD_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_NAME_RECORD_ID + ") REFERENCES " +
                RecordTable.TABLE_NAME + "(" + RecordTable._ID + "));";

        static final String[] COLUMNS = {_ID, COLUMN_NAME_QUESTION, COLUMN_NAME_ANSWER, COLUMN_NAME_RECORD_ID};
    }

}
