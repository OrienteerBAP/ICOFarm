package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class CryptocurrencyWallet extends ExternalWallet {
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "CryptocurrencyWallet";

	public CryptocurrencyWallet(ODocument wallet) {
		super(wallet);
	}

}
