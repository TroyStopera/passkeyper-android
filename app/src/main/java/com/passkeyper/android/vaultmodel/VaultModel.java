package com.passkeyper.android.vaultmodel;

import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

/**
 * Abstract model class for data that is stored within a Vault.
 */
public abstract class VaultModel implements Parcelable {

    /* The unique ID of a model */
    private long id = -1;

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

    /**
     * Provides a way for the VaultManager to change the Model's id when updated in a DB without
     * exposing a 'setId()' method.
     */
    public static abstract class Manager {

        protected void setModelID(VaultModel model, long id) {
            model.id = id;
        }

    }

    /**
     * Holds all secure string data from VaultModels. This allows for no copies of secure strings
     * to be made when models are parcelled.
     */
    static final class SecureStringData {

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
            if (contains(id)) {
                char[] string = get(id);
                for (int i = 0; i < string.length; i++)
                    string[i] = Character.MIN_VALUE;
            }
        }

        void eraseAll() {
            for (int i = 0; i < strings.size(); i++)
                erase(strings.keyAt(i));
        }

    }

}
