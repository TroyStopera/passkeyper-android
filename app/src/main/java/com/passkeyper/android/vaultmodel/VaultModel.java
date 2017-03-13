package com.passkeyper.android.vaultmodel;

/**
 * Abstract model class for data that is stored within a Vault.
 */
public abstract class VaultModel {

    private long id = -1;

    /**
     * @return The id of the Vault model.
     */
    public final long getId() {
        return id;
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

}
