package com.passkeyper.android.vault;

import com.passkeyper.android.vaultmodel.EntryRecord;
import com.passkeyper.android.vaultmodel.SecurityQuesEntry;
import com.passkeyper.android.vaultmodel.SensitiveEntry;
import com.passkeyper.android.vaultmodel.VaultModel;

import java.util.List;

/**
 * Defines the methods that need to be implemented in order to manage a vault.
 */
public abstract class VaultManager extends VaultModel.Manager {

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
     * Delete an EntryRecord from the vault.
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
