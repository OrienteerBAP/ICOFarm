package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

public class OWallet extends ODocumentWrapper{
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "OWallet";

	public static final String OPROPERTY_BALANCE = "balance";

	public OWallet(ODocument wallet) {
		super(wallet);
	}

	public OWallet(String iClassName) {
		super(iClassName);
	}

	public String getBalance() {
		return document.field(OPROPERTY_BALANCE);
	}

	public OWallet setBalance(String balance) {
		document.field(OPROPERTY_BALANCE, balance);
		return this;
	}

	public OWallet sudoSave() {
		DBClosure.sudoSave(this);
		return this;
	}
}
