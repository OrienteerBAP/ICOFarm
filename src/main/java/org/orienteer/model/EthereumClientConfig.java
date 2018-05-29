package org.orienteer.model;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

public class EthereumClientConfig extends ODocumentWrapper {

    public static final String OPROPERTY_NAME                      = "name";
    public static final String OPROPERTY_HOST                      = "host";
    public static final String OPROPERTY_PORT                      = "port";
    public static final String OPROPERTY_TIMEOUT                   = "timeout";
    public static final String OPROPERTY_TRANSACTIONS_BUFFER_DELAY = "transactionsBufferDelay";
    public static final String OPROPERTY_TRANSACTIONS_BUFFER_NUM   = "transactionsBufferNum";
    public static final String OPROPERTY_MAIN_TOKEN                = "mainToken";

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

    public Token getMainToken() {
        OIdentifiable doc = document.field(OPROPERTY_MAIN_TOKEN);
        if (doc instanceof ORecordId) {
            doc = new ODocument((ORecordId) doc);
        }
        return doc != null ? new Token((ODocument) doc) : null;
    }

    @Override
    public <RET extends ODocumentWrapper> RET save() {
        throw new IllegalStateException("Can't save config model!");
    }

    @Override
    public <RET extends ODocumentWrapper> RET save(String iClusterName) {
        throw new IllegalStateException("Can't save config model!");
    }
}
