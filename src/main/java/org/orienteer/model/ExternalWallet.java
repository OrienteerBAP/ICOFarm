package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class ExternalWallet extends Wallet {

	public static final String CLASS_NAME = "ExternalWallet";

	public ExternalWallet(ODocument wallet) {
		super(wallet);
	}

	private static final long serialVersionUID = 1L;

}
