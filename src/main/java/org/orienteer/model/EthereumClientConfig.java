package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthereumClientConfig extends ODocumentWrapper {

    public static final String OPROPERTY_NAME                      = "name";
    public static final String OPROPERTY_HOST                      = "host";
    public static final String OPROPERTY_PORT                      = "port";
    public static final String OPROPERTY_TIMEOUT                   = "timeout";
    public static final String OPROPERTY_TRANSACTIONS_BUFFER_DELAY = "transactionsBufferDelay";
    public static final String OPROPERTY_TRANSACTIONS_BUFFER_NUM   = "transactionsBufferNum";
    public static final String OPROPERTY_MAIN_TOKEN_CURRENCY       = "mainTokenCurrency";


    public EthereumClientConfig(ODocument doc) {
        super(doc);
    }

    public String getName() {
        return document.field(OPROPERTY_NAME);
    }

    public String getHost() {
        return document.field(OPROPERTY_HOST);
    }

    public int getPort() {
        return document.field(OPROPERTY_PORT);
    }

    public int getTimeout() {
        return document.field(OPROPERTY_TIMEOUT);
    }

    public int getTransactionsBufferDelay(){
    	return document.field(OPROPERTY_TRANSACTIONS_BUFFER_DELAY);
    }
    
    public int getTransactionsBufferSize(){
    	return document.field(OPROPERTY_TRANSACTIONS_BUFFER_NUM);
    }

    public TokenCurrency getMainTokenCurrency(){
    	return new TokenCurrency(document.field(OPROPERTY_MAIN_TOKEN_CURRENCY));
    }

    public BigInteger getGasPriceFor(Currency currency) {
        return Convert.toWei(BigDecimal.ONE, Convert.Unit.GWEI).toBigInteger();
    }

    public BigInteger getGasLimitFor(Currency currency) {
        return BigInteger.valueOf(200000);
    }
    
    @Override
    public <RET extends ODocumentWrapper> RET save() {
        throw new IllegalStateException("Can't save config model!");
    }

    @Override
    public <RET extends ODocumentWrapper> RET save(String iClusterName) {
        throw new IllegalStateException("Can;t save config model!");
    }
}
