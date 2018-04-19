package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class TokenCurrency extends Currency {
	private static final long serialVersionUID = 1L;
	
	public static final String CLASS_NAME = "TokenCurrency";

	public static final String OPROPERTY_ADDRESS = "address";

	public TokenCurrency(ODocument currencyDoc) {
		super(currencyDoc);
	}

	public String getContractAddress() {
		return document.field(OPROPERTY_ADDRESS);
	}
}
