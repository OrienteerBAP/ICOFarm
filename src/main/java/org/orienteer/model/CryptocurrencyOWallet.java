package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class CryptocurrencyOWallet extends ExternalOWallet {
	private static final long serialVersionUID = 1L;

	public CryptocurrencyOWallet(ODocument wallet) {
		super(wallet);
	}

}
