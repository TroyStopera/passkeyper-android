package com.passkeyper.android.vaultmodel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents the 'public' data for an entry in the Vault. Only contains the name of the
 * account/entry and optionally  the username.
 */

public class EntryRecord extends VaultModel {

    /**
     * The Parcelable Creator for EntryRecord.
     */
    public static final Parcelable.Creator<EntryRecord> CREATOR = new Parcelable.Creator<EntryRecord>() {
        public EntryRecord createFromParcel(Parcel in) {
            return new EntryRecord(in);
        }

        public EntryRecord[] newArray(int size) {
            return new EntryRecord[size];
        }
    };

    /* The account name and username */
    private String account, username;

    /**
     * Creates a new, empty EntryRecord.
     */
    public EntryRecord() {

    }

    /* Create from Parcel */
    private EntryRecord(Parcel in) {
        account = in.readString();
        username = in.readString();
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(account);
        parcel.writeString(username);
    }

}
