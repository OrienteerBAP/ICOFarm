package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import java.util.Date;

public class OTransaction extends ODocumentWrapper {

    public static final String CLASS_NAME = "OTransaction";

    public static final String OPROPERTY_TIMESTAMP = "timestamp";
    public static final String OPROPERTY_FROM      = "from";
    public static final String OPROPERTY_TO        = "to";
    public static final String OPROPERTY_VALUE     = "value";
    public static final String OPROPERTY_HASH      = "hash";
    public static final String OPROPERTY_BLOCK     = "block";
    public static final String OPROPERTY_CONFIRMED = "confirmed";

	private static final long serialVersionUID = 1L;

	public OTransaction() {
        super(CLASS_NAME);
    }

    public OTransaction(ODocument iDocument) {
        super(iDocument);
    }

    public OTransaction setTimestamp(Date timestamp) {
	    document.field(OPROPERTY_TIMESTAMP, timestamp);
	    return this;
    }

    public Date getTimestamp() {
	    return document.field(OPROPERTY_TIMESTAMP);
    }

    public OTransaction setFrom(String from) {
	    document.field(OPROPERTY_FROM, from);
	    return this;
    }

    public String getFrom() {
	    return document.field(OPROPERTY_FROM);
    }

    public OTransaction setTo(String to) {
	    document.field(OPROPERTY_TO, to);
	    return this;
    }

    public String getValue() {
	    return document.field(OPROPERTY_VALUE);
    }

    public OTransaction setValue(String value) {
	    document.field(OPROPERTY_VALUE, value);
	    return this;
    }

    public OTransaction setHash(String hash) {
	    document.field(OPROPERTY_HASH, hash);
	    return this;
    }

    public String getHash() {
	    return document.field(OPROPERTY_HASH);
    }

    public OTransaction setBlock(String block) {
	    document.field(OPROPERTY_BLOCK, block);
	    return this;
    }

    public String getBlock() {
	    return document.field(OPROPERTY_BLOCK);
    }

    public OTransaction setConfirmed(boolean confirmed) {
	    document.field(OPROPERTY_CONFIRMED, confirmed);
	    return this;
    }

    public boolean isConfirmed() {
	    Boolean confirmed = document.field(OPROPERTY_CONFIRMED);
	    return confirmed != null && confirmed;
    }

}
