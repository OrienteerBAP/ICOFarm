package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class ExternalWallet extends Wallet{
	public ExternalWallet(ODocument wallet) {
		super(wallet);
	}

	private static final long serialVersionUID = 1L;

}
