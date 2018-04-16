package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class EthereumWallet extends CryptocurrencyWallet {
	private static final long serialVersionUID = 1L;

	public static final String CACHE_FOLDER = System.getProperty("java.io.tmpdir")+"/ETHCache";//"temp/";

	public static final String CLASS_NAME = "EthereumWallet";
	
	public EthereumWallet(ODocument wallet) {
		super(wallet);
	}

}
