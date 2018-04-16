package org.orienteer.model;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Wallet extends ODocumentWrapper {
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "Wallet";

	public static final String OPROPERTY_OWNER        = "owner";
	public static final String OPROPERTY_NAME         = "name";
	public static final String OPROPERTY_CURRENCY     = "currency";
	public static final String OPROPERTY_BALANCE      = "balance";
	public static final String OPROPERTY_ADDRESS      = "address";
	public static final String OPROPERTY_CREATED      = "created";
	public static final String OPROPERTY_TRANSACTIONS = "transactions";
	public static final String OPROPERTY_WALLET_JSON  = "walletJSON";

	public Wallet(ODocument wallet) {
		super(wallet);
	}

	public Wallet() {
		super(CLASS_NAME);
	}

	public String getBalance() {
		return document.field(OPROPERTY_BALANCE);
	}

	public Wallet setBalance(String balance) {
		document.field(OPROPERTY_BALANCE, balance);
		return this;
	}

	public String getAddress() {
		return document.field(OPROPERTY_ADDRESS);
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

	public Currency getCurrency() {
		return new Currency((ODocument) document.field(OPROPERTY_CURRENCY));
	}

	public Wallet setWalletJSON(byte [] data) {
		document.field(OPROPERTY_WALLET_JSON, data);
		return this;
	}

	public byte[] getWalletJSON(){
		byte [] data = getDocument().field(OPROPERTY_WALLET_JSON);
		return data != null ? data : new byte[0];
	}

	public String getWalletJSONName(){
		return getDocument().field(OPROPERTY_WALLET_JSON + ".json");
	}

}
