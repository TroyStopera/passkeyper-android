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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * Returns the password being used by this VaultManager.
     *
     * @return the password.
     */
    public abstract char[] getPassword();

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

    /**
     * Returns the recovery data needed to access the vault when the password is forgotten.
     *
     * @return the RecoveryDta.
     */
    public abstract RecoveryData getRecoveryData();

    /**
     * Saves the recovery data to the vault.
     *
     * @param recoveryData the RecoveryData to save.
     */
    public abstract void updateRecoveryData(RecoveryData recoveryData);

    /**
     * Changes the password for the vault this VaultManager manages.
     *
     * @param password teh new password.
     */
    public abstract void changePassword(char[] password);

    /**
     * Interface used to handle data that is needed to recover the Vault if the password is forgotten.
     */
    public interface RecoveryData {

        /**
         * Get the vault's security question.
         *
         * @return the security question.
         */
        String getSecurityQuestion();

        /**
         * Sets the security question for the vault.
         *
         * @param securityQuestion the security question.
         */
        void setSecurityQuestion(String securityQuestion);

        /**
         * Get the vault's security question answer.
         *
         * @return the security question answer.
         */
        char[] getSecurityAnswer();

        /**
         * sets the security question answer for the vault.
         *
         * @param securityAnswer the security question answer.
         */
        void setSecurityAnswer(char[] securityAnswer);

        /**
         * Frees the security question answer from memory.
         */
        void free();

    }

}
