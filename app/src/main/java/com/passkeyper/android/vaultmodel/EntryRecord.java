package com.passkeyper.android.vaultmodel;

/**
 * Represents the 'public' data for an entry in the Vault. Only contains the name of the
 * account/entry and optionally  the username.
 */

public class EntryRecord extends VaultModel {

    private String account, username;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
