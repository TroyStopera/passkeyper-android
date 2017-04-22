package com.passkeyper.android.vaultmodel;

/**
 * Marker interface to group models that contain sensitive data together.
 */
public interface PrivateModel extends VaultModel {

    /**
     * Deletes this model's private data from memory.
     */
    void free();

}
