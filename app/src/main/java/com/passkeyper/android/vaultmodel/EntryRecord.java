package com.passkeyper.android.vaultmodel;

import android.os.Parcel;

/**
 * Represents the 'public' data for an entry in the Vault. Only contains the name of the
 * account/entry and optionally  the username.
 */
public class EntryRecord extends VaultModel {

    /**
     * The Parcelable Creator for EntryRecord.
     */
    public static final Creator<EntryRecord> CREATOR = new Creator<EntryRecord>() {

        protected EntryRecord newFromParcel(Parcel parcel) {
            return new EntryRecord(parcel);
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
    protected void saveToParcel(Parcel parcel, int i) {
        parcel.writeString(account);
        parcel.writeString(username);
    }

}
