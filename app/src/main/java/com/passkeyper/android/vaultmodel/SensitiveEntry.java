package com.passkeyper.android.vaultmodel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents the 'private' data for an entry in the Vault. Contains the name of a piece of
 * sensitive data and teh value that goes along with it.
 */

public class SensitiveEntry extends VaultModel {

    /**
     * The Parcelable Creator for SecurityQuesEntry.
     */
    public static final Parcelable.Creator<SensitiveEntry> CREATOR = new Parcelable.Creator<SensitiveEntry>() {
        public SensitiveEntry createFromParcel(Parcel in) {
            return new SensitiveEntry(in);
        }

        public SensitiveEntry[] newArray(int size) {
            return new SensitiveEntry[size];
        }
    };

    /* The 'name' of the sensitive data and the value of it */
    private String name, value;
    /* The record this sensitive data corresponds to */
    private final EntryRecord record;

    /**
     * Creates a new, empty SensitiveEntry associated with the given EntryRecord.
     *
     * @param record the EntryRecord that this SensitiveEntry is associated with.
     */
    public SensitiveEntry(EntryRecord record) {
        this.record = record;
    }

    /* Create from Parcel */
    private SensitiveEntry(Parcel in) {
        record = in.readParcelable(EntryRecord.class.getClassLoader());
        name = in.readString();
        value = in.readString();
    }

    public EntryRecord getRecord() {
        return record;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(record, i);
        parcel.writeString(name);
        parcel.writeString(value);
    }

}
