package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class EmbeddedWallet extends Wallet {

    public static final String CLASS_NAME = "EmbeddedWallet";

    public static final String OPROPERTY_PASSWORD = "password";
    public static final String OPROPERTY_NAME     = "name";

    public EmbeddedWallet(ODocument wallet) {
        super(wallet);
    }

    public EmbeddedWallet() {
        super(CLASS_NAME);
    }

    public String getPassword() {
        return document.field(OPROPERTY_PASSWORD);
    }

    public String getName() {
        return document.field(OPROPERTY_NAME);
    }

}
