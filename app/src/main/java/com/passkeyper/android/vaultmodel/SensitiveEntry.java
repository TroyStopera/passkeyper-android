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

    /* Data structure that holds all 'value' strings for security reasons */
    private static final SecureStringData strings = new SecureStringData();

    /* The 'name' of the sensitive data */
    private String name;
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

    public char[] getValue() {
        if (strings.contains(getId()))
            return strings.get(getId());
        else
            throw new IllegalStateException("SensitiveEntry value has been accessed after it has been erased from memory");
    }

    public void setValue(char[] value) {
        strings.put(value, getId());
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(record, i);
        parcel.writeString(name);
    }

    /**
     * Erases all of the char[]'s in memory that contain sensitive data.
     */
    public static void eraseSecureMemory() {
        strings.eraseAll();
    }

}
