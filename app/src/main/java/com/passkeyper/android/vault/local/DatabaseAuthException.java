package com.passkeyper.android.vault.local;

/**
 * Exception that is thrown when there is an authentication problem when loading the local database.
 */
public class DatabaseAuthException extends Exception {

    DatabaseAuthException(String message) {
        super(message);
    }

}
