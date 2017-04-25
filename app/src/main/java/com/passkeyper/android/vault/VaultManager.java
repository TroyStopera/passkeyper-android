package com.passkeyper.android.vault;

import com.passkeyper.android.vaultmodel.AbstractVaultModel;
import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.vaultmodel.VaultModel;

import java.util.List;

/**
 * Defines the methods that need to be implemented in order to manage a vault.
 */
public abstract class VaultManager extends AbstractVaultModel.Manager {

    private boolean isClosed = false;

    /**
     * Saves a VaultModel to the vault.
     *
     * @param vaultModel the model to save.
     */
    public final void save(VaultModel vaultModel) {
        if (vaultModel instanceof EntryRecord) save((EntryRecord) vaultModel);
        else if (vaultModel instanceof SensitiveEntry) save((SensitiveEntry) vaultModel);
        else if (vaultModel instanceof SecurityQuesEntry) save((SecurityQuesEntry) vaultModel);
    }

    /**
     * Deletes a VaultModel from the vault.
     *
     * @param vaultModel the model to delete.
     */
    public final void delete(VaultModel vaultModel) {
        if (vaultModel instanceof EntryRecord) delete((EntryRecord) vaultModel);
        else if (vaultModel instanceof SensitiveEntry) delete((SensitiveEntry) vaultModel);
        else if (vaultModel instanceof SecurityQuesEntry) delete((SecurityQuesEntry) vaultModel);
    }

    /**
     * Closes a vault. Once closed, a vault should not be able to do any transactions.
     */
    public void close() {
        isClosed = true;
    }

    /**
     * @return true if this VaultManager has been closed.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * @return all EntryRecords from the vault.
     */
    public abstract List<EntryRecord> getAllEntryRecords();

    /**
     * @param record the EntryRecord that the requested SensitiveEntries should be associated with.
     * @return a List of SensitiveData.
     */
    public abstract List<SensitiveEntry> getSensitiveEntries(EntryRecord record);

    /**
     * @param record the EntryRecord that the requested SecurityQuesEntries should be associated with.
     * @return a List of SecurityQuesEntry.
     */
    public abstract List<SecurityQuesEntry> getSecurityQuestions(EntryRecord record);

    /**
     * Saves an EntryRecord to the vault.
     *
     * @param record the EntryRecord to save.
     */
    public abstract void save(EntryRecord record);

    /**
     * Saves a SensitiveEntry to the vault.
     *
     * @param sensitiveEntry the SensitiveEntry to save.
     */
    public abstract void save(SensitiveEntry sensitiveEntry);

    /**
     * Saves a SecurityQuesEntry to the vault.
     *
     * @param securityQuesEntry the SecurityQuesEntry to save.
     */
    public abstract void save(SecurityQuesEntry securityQuesEntry);

    /**
     * Delete an EntryRecord from the vault. Implementations should also delete all related database rows.
     *
     * @param record the EntryRecord to delete.
     */
    public abstract void delete(EntryRecord record);

    /**
     * Delete a SensitiveEntry from the vault.
     *
     * @param sensitiveEntry the SensitiveEntry to delete.
     */
    public abstract void delete(SensitiveEntry sensitiveEntry);

    /**
     * Delete a SecurityQuesEntry from the vault.
     *
     * @param securityQuesEntry the SecurityQuesEntry to delete.
     */
    public abstract void delete(SecurityQuesEntry securityQuesEntry);

}
