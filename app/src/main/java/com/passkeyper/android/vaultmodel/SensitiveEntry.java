package com.passkeyper.android.vaultmodel;

import android.os.Parcel;

/**
 * Represents the 'private' data for an entry in the Vault. Contains the name of a piece of
 * sensitive data and teh value that goes along with it.
 */

public class SensitiveEntry extends VaultModel {

    /**
     * The Parcelable Creator for SecurityQuesEntry.
     */
    public static final Creator<SensitiveEntry> CREATOR = new Creator<SensitiveEntry>() {
        public SensitiveEntry newFromParcel(Parcel parcel) {
            return new SensitiveEntry(parcel);
        }

        public SensitiveEntry[] newArray(int size) {
            return new SensitiveEntry[size];
        }
    };

    /* Data structure that holds all 'value' strings for security reasons */
    private static final SecureStringData strings = new SecureStringData();

    /* The 'name' of the sensitive data */
    private String name;
    /* Keeps track of if a value has been set yet */
    private boolean valueSet = false;
    /* Keeps track of if the data has been erased */
    private boolean erased = false;
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
        valueSet = in.readByte() == 1;
        erased = in.readByte() == 1;
    }

    public EntryRecord getRecord() {
        return record;
    }

    public boolean hasName() {
        return name != null && !name.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasValue() {
        return valueSet && !erased;
    }

    public char[] getValue() {
        //if the value has never been set return an empty array
        if (!valueSet) return new char[0];
        else if (strings.contains(key))
            return strings.get(key);
        else
            throw new IllegalStateException("SensitiveEntry value has been accessed after it has been erased from memory");
    }

    public void setValue(char[] value) {
        if (erased)
            throw new IllegalStateException("SensitiveEntry value has been accessed after it has been erased from memory");

        valueSet = true;
        strings.put(value, key);
    }

    public void free() {
        strings.erase(key);
    }

    @Override
    protected void saveToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(record, i);
        parcel.writeString(name);
        parcel.writeByte((byte) (valueSet ? 1 : 0));
        parcel.writeByte((byte) (erased ? 1 : 0));
    }

    /**
     * Erases all of the char[]'s in memory that contain sensitive data.
     */
    public static void eraseSecureMemory() {
        strings.eraseAll();
    }

}
