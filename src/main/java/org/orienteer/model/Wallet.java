package org.orienteer.model;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.apache.commons.collections4.map.HashedMap;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Wallet extends ODocumentWrapper {
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "Wallet";

	public static final String OPROPERTY_OWNER             = "owner";
	public static final String OPROPERTY_NAME              = "name";
	public static final String OPROPERTY_ADDRESS           = "address";
	public static final String OPROPERTY_CREATED           = "created";
	public static final String OPROPERTY_TRANSACTIONS      = "transactions";
	public static final String OPROPERTY_WALLET_JSON       = "walletJSON";
	public static final String OPROPERTY_DISPLAYABLE_TOKEN = "displayableToken";
	public static final String OPROPERTY_BALANCES = "balances";

	public Wallet(ODocument wallet) {
		super(wallet);
	}

	public Wallet() {
		super(CLASS_NAME);
	}

	public String getName() {
		return document.field(OPROPERTY_NAME);
	}

	public Wallet setName(String name) {
		document.field(OPROPERTY_NAME, name);
		return this;
	}

	public String getAddress() {
		return document.field(OPROPERTY_ADDRESS);
	}

	public Wallet setAddress(String address) {
		document.field(OPROPERTY_ADDRESS, address);
		return this;
	}

	public ODocument getOwner() {
		return (ODocument) document.field(OPROPERTY_OWNER);
	}

	public Wallet setOwner(ODocument doc) {
		document.field(OPROPERTY_OWNER, doc);
		return this;
	}

	public Date getCreated() {
		return document.field(OPROPERTY_CREATED);
	}

	public Wallet setCreated(Date created) {
		document.field(OPROPERTY_CREATED, created);
		return this;
	}

	public List<OTransaction> getTransactions() {
		List<OIdentifiable> transactions = document.field(OPROPERTY_TRANSACTIONS);
		return transactions == null || transactions.isEmpty() ? Collections.emptyList() :
				transactions.stream()
						.map(t -> (ODocument) t.getRecord())
						.map(OTransaction::new)
						.collect(Collectors.toList());
	}

	public Wallet setTransactions(List<OTransaction> transactions) {
		List<ODocument> docs = transactions.stream().map(OTransaction::getDocument).collect(Collectors.toList());
		document.field(OPROPERTY_TRANSACTIONS, docs);
		return this;
	}

	public Wallet setWalletJSON(byte [] data) {
		document.field(OPROPERTY_WALLET_JSON, data);
		return this;
	}

	public byte[] getWalletJSON(){
		byte [] data = getDocument().field(OPROPERTY_WALLET_JSON);
		return data != null ? data : new byte[0];
	}

	public Token getDisplayableToken() {
		return new Token((ODocument) document.field(OPROPERTY_DISPLAYABLE_TOKEN));
	}

	public Wallet setDisplayableToken(ODocument token) {
		document.field(OPROPERTY_DISPLAYABLE_TOKEN, token);
		return this;
	}

	public String getWalletJSONName(){
		return getDocument().field(OPROPERTY_WALLET_JSON + ".json");
	}

	public Map<String, BigDecimal> getBalances() {
		return document.field(OPROPERTY_BALANCES);
	}

	public BigDecimal getBalance(String symbol) {
		Map<String, BigDecimal> balances = getBalances();
		return balances != null ? balances.get(symbol) : BigDecimal.ZERO;
	}

	public Wallet setBalance(String symbol, BigDecimal wei) {
		Map<String, BigDecimal> balances = getBalances();
		if (balances == null) {
			balances = new HashedMap<>();
		}
		balances.put(symbol, wei);
		setBalances(balances);
		return this;
	}

	public Wallet setBalances(Map<String, BigDecimal> balances) {
		document.field(OPROPERTY_BALANCES, balances);
		return this;
	}


}
