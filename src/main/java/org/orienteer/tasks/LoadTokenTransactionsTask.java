package org.orienteer.tasks;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.tasks.OTask;
import org.orienteer.core.tasks.OTaskSessionRuntime;
import org.orienteer.model.Token;
import org.orienteer.model.TransferEvent;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.service.web3.IICOFarmSmartContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.utils.Numeric;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoadTokenTransactionsTask extends OTask {

    private static final Logger LOG = LoggerFactory.getLogger(LoadTokenTransactionsTask.class);

    public static final String CLASS_NAME = "LoadTokenTransactionsTask";

    public static final String OPROPERTY_START_BLOCK = "startBlock";
    public static final String OPROPERTY_END_BLOCK   = "endBlock";
    public static final String OPROPERTY_TOKEN       = "tokenAddress";

    public static final String NAME_PREFIX = "loadTransactions_";

    public static LoadTokenTransactionsTask create(Token token, boolean deleteOnFinish) {
        ODocument doc = new ODocument(CLASS_NAME);
        doc.field(OTask.Field.AUTODELETE_SESSIONS.fieldName(), deleteOnFinish);
        return new LoadTokenTransactionsTask(doc)
                .setToken(token);
    }

    public LoadTokenTransactionsTask(ODocument oTask) {
        super(oTask);
    }

    @Override
    public OTaskSessionRuntime startNewSession() {
        OTaskSessionRuntime session = new OTaskSessionRuntime();
        session.setDeleteOnFinish(document.field(Field.AUTODELETE_SESSIONS.fieldName()));
        session.setOTask(this);

        IEthereumService ethService = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
        Token token = getToken();
        IICOFarmSmartContract smartContract = ethService.loadSmartContract(token.getOwner().getAddress(), token);
        startLoadingTokenTransactions(smartContract, session);
        return session;
    }

    private void startLoadingTokenTransactions(IICOFarmSmartContract smartContract, OTaskSessionRuntime session) {
        smartContract.transferEventObservable(getStartBlock(), getEndBlock())
                .subscribeOn(Schedulers.io())
                .buffer(5, TimeUnit.SECONDS, 100) // user must see progress of transactions loading
                .doOnSubscribe(session::start)
                .subscribe(createSubscriber(session));
    }

    private Subscriber<List<TransferEvent>> createSubscriber(OTaskSessionRuntime session) {
        IDBService dbService = OrienteerWebApplication.get().getServiceInstance(IDBService.class);

        return new Subscriber<List<TransferEvent>>() {
            @Override
            public void onCompleted() {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
                session.finish();
            }

            @Override
            public void onError(Throwable err) {
                LOG.error("Error during loading transactions for {}", getToken(), err);
            }

            @Override
            public void onNext(List<TransferEvent> transferEvents) {
                if (!transferEvents.isEmpty()) {
                    dbService.saveTransactionsFromTransferEvents(transferEvents);
                } else onCompleted();
            }
        };
    }

    public DefaultBlockParameter getStartBlock() {
        return toDefaultBlockParameter(document.field(OPROPERTY_START_BLOCK));
    }

    public LoadTokenTransactionsTask setStartBlock(DefaultBlockParameter startBlock) {
        document.field(OPROPERTY_START_BLOCK, startBlock.getValue());
        return this;
    }

    public DefaultBlockParameter getEndBlock() {
        return toDefaultBlockParameter(document.field(OPROPERTY_END_BLOCK));
    }

    public LoadTokenTransactionsTask setEndBlock(DefaultBlockParameter endBlock) {
        document.field(OPROPERTY_END_BLOCK, endBlock.getValue());
        return this;
    }

    public Token getToken() {
        OIdentifiable doc = document.field(OPROPERTY_TOKEN);
        if (doc instanceof ORecordId) {
            doc = new ODocument((ORecordId) doc);
        }
        return new Token((ODocument) doc);
    }

    public LoadTokenTransactionsTask setToken(Token token) {
        setNameFromToken(token);
        return setToken(token.getDocument());
    }

    public LoadTokenTransactionsTask setToken(ODocument token) {
        document.field(OPROPERTY_TOKEN, token);
        return this;
    }

    public LoadTokenTransactionsTask setNameFromToken(Token token) {
        document.field(Field.NAME.fieldName(), NAME_PREFIX + token.getSymbol());
        return this;
    }

    private DefaultBlockParameter toDefaultBlockParameter(String value) {
        try {
            return DefaultBlockParameter.valueOf(Numeric.toBigInt(value));
        } catch (NumberFormatException ex) {

        }
        return DefaultBlockParameter.valueOf(value);
    }
}
