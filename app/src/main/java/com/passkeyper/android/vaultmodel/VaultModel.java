package com.passkeyper.android.vaultmodel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

import java.util.Arrays;

/**
 * Abstract model class for data that is stored within a Vault.
 */
public abstract class VaultModel implements Parcelable {

    /* The unique ID of a model */
    private long id = -1;

    /**
     * @return true if this model has been written to the vault db.
     */
    public final boolean isSaved() {
        return id != -1;
    }

    /**
     * Write the model's data to the given Parcel.
     *
     * @param parcel the Parcel to write to.
     * @param i      flags.
     */
    protected abstract void saveToParcel(Parcel parcel, int i);

    /**
     * @return the id of the Vault model.
     */
    public final long getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel parcel, int i) {
        saveToParcel(parcel, i);
        parcel.writeLong(id);
    }

    /**
     * Provides a way for the VaultManager to change the Model's id when updated in a DB without
     * exposing a 'setId()' method.
     */
    public static abstract class Manager {

        protected final void setModelID(VaultModel model, long id) {
            model.id = id;
        }

    }

    /**
     * Holds all secure string data from VaultModels. This allows for no copies of secure strings
     * to be made when models are parcelled.
     */
    static final class SecureStringData {

        //TODO: fix bug where unsaved models will all map to the same string

        private final LongSparseArray<char[]> strings = new LongSparseArray<>();

        /* Only instantiate from classes in vaultmodel */
        SecureStringData() {

        }

        void put(char[] string, long id) {
            //erase any old data for this id
            erase(id);
            //put the new data in
            strings.put(id, string);
        }

        boolean contains(long id) {
            return strings.get(id) != null;
        }

        char[] get(long id) {
            return strings.get(id);
        }

        void erase(long id) {
            if (contains(id))
                Arrays.fill(get(id), '\0');
        }

        void eraseAll() {
            for (int i = 0; i < strings.size(); i++)
                erase(strings.keyAt(i));
        }

    }

    /**
     * A partial implementation of Parcelable.Creator that handles reading and setting a models id.
     *
     * @param <T> the type of VaultModel this Creator creates.
     */
    static abstract class Creator<T extends VaultModel> implements Parcelable.Creator<VaultModel> {

        /**
         * Create a new modle from the given Parcel.
         *
         * @param parcel the Parcel with the model's data.
         * @return a new model with the given data from parcel.
         */
        protected abstract T newFromParcel(Parcel parcel);

        /**
         * Create a new array.
         *
         * @param size the size of the new array.
         * @return a new array.
         */
        @Override
        public abstract T[] newArray(int size);

        @Override
        public final VaultModel createFromParcel(Parcel parcel) {
            VaultModel model = newFromParcel(parcel);
            model.id = parcel.readLong();
            return model;
        }

    }

}
