package org.orienteer.service.web3;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.service.IDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Singleton
public class EthereumUpdateServiceImpl implements IEthereumUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumUpdateServiceImpl.class);

    @Inject
    private IEthereumService ethereumService;

    @Inject
    private IDBService dbService;

    private CompositeSubscription compositeSubscription;

    @Override
    public void init(ODocument config) {
        if (compositeSubscription == null || !compositeSubscription.hasSubscriptions()) {
            if (compositeSubscription == null) compositeSubscription = new CompositeSubscription();
            ethereumService.init(config);

            compositeSubscription.addAll(
                    subscribeOnPendingTransactions(),
                    subscribeOnUpdateTransactions()
            );
        }
    }

    @Override
    public void destroy() {
        compositeSubscription.clear();
        ethereumService.destroy();
    }

    private Subscription subscribeOnUpdateTransactions() {
        EthereumClientConfig config = ethereumService.getConfig();
        Observable<List<Transaction>> obs = ethereumService.getTransactionsObservable()
        	.buffer(config.getTransactionsBufferDelay(), TimeUnit.SECONDS, config.getTransactionsBufferSize())
        	.subscribeOn(Schedulers.io());

        Function<Transaction, EthBlock.Block> blockFunction = transaction -> {
            EthBlock result = null;
            try {
                result = ethereumService.requestBlock(transaction.getBlockNumberRaw());
            } catch (Exception ex) {
                LOG.error("Can't get transaction block: {}", transaction, ex);
            }
            return result != null ? result.getBlock() : null;
        };

        return obs.distinct().subscribe(transactions-> dbService.confirmICOFarmTransactions(transactions, blockFunction),
                (t) -> LOG.error("Can't receive new transactions!", t));
    }

    private Subscription subscribeOnPendingTransactions() {
        EthereumClientConfig config = ethereumService.getConfig();
        Observable<List<Transaction>> obs = ethereumService.getPendingTransactionsObservable()
                .buffer(config.getTransactionsBufferDelay(), TimeUnit.SECONDS, config.getTransactionsBufferSize())
                .subscribeOn(Schedulers.io());

        return obs.distinct().subscribe(transactions -> dbService.saveUnconfirmedICOFarmTransactions(transactions),
                (t) -> LOG.error("Can't receive pending transactions!", t));
    }

}
