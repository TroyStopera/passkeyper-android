package com.passkeyper.android.vaultmodel;

/**
 * Represents the 'private' data for an entry in the Vault. Contains the name of a piece of
 * sensitive data and teh value that goes along with it.
 */

public class SensitiveEntry extends VaultModel {

    private String name, value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
