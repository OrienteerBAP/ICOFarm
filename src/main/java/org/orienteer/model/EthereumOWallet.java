package org.orienteer.model;

import org.orienteer.core.component.property.BinaryEditPanel;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class EthereumOWallet extends CryptocurrencyOWallet {
	private static final long serialVersionUID = 1L;

	public static final String CACHE_FOLDER = System.getProperty("java.io.tmpdir")+"/ETHCache";//"temp/";

	public static final String WALLET_JSON         = "walletJSON";
    
	
	public EthereumOWallet(ODocument wallet) {
		super(wallet);
	}

	public String getWalletJSON(){
		byte[] data = getDocument().field(WALLET_JSON);
		return data.length>0?new String(data):null;
	}
	public String getWalletJSONName(){
		return getDocument().field(WALLET_JSON+BinaryEditPanel.FILENAME_SUFFIX);
	}
	
}
