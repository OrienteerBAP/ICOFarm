package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class EmbeddedOWallet extends OWallet {

    public static final String CLASS_NAME = "EmbeddedWallet";

    public static final String OPROPERTY_PASSWORD = "password";
    public static final String OPROPERTY_NAME     = "name";
    public static final String OPROPERTY_ADDRESS  = "address";

    public EmbeddedOWallet(ODocument wallet) {
        super(wallet);
    }

    public EmbeddedOWallet() {
        super(CLASS_NAME);
    }

    public String getPassword() {
        return document.field(OPROPERTY_PASSWORD);
    }

    public String getName() {
        return document.field(OPROPERTY_NAME);
    }

    public String getAddress() {
        return document.field(OPROPERTY_ADDRESS);
    }

}
