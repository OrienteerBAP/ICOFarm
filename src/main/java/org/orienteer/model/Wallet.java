package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

public class Wallet extends ODocumentWrapper {
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "Wallet";

	public static final String OPROPERTY_OWNER    = "owner";
	public static final String OPROPERTY_CURRENCY = "currency";
	public static final String OPROPERTY_BALANCE  = "balance";
	public static final String OPROPERTY_ADDRESS  = "address";

	public Wallet(ODocument wallet) {
		super(wallet);
	}

	public Wallet(String iClassName) {
		super(iClassName);
	}

	public String getBalance() {
		return document.field(OPROPERTY_BALANCE);
	}

	public Wallet setBalance(String balance) {
		document.field(OPROPERTY_BALANCE, balance);
		return this;
	}

	public String getAddress() {
		return document.field(OPROPERTY_ADDRESS);
	}

	public ODocument getOwner() {
		return (ODocument) document.field(OPROPERTY_OWNER);
	}

	public Wallet setOwner(ODocument doc) {
		document.field(OPROPERTY_OWNER, doc);
		return this;
	}

	public Currency getCurrency() {
		return new Currency((ODocument) document.field(OPROPERTY_CURRENCY));
	}

	public Wallet setCurrencu(ODocument doc) {
		document.field(OPROPERTY_CURRENCY, doc);
		return this;
	}

	public Wallet sudoSave() {
		DBClosure.sudoSave(this);
		return this;
	}
}
