package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

public class Wallet extends ODocumentWrapper{
	private static final long serialVersionUID = 1L;

	public Wallet(ODocument wallet) {
		super(wallet);
	}
}
