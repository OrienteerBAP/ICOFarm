package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.math.BigInteger;
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
        if (compositeSubscription == null || compositeSubscription.isUnsubscribed()) {
            if (compositeSubscription == null) compositeSubscription = new CompositeSubscription();
            ethereumService.init(config);

            compositeSubscription.addAll(
                    subscribeOnUpdateBalanceByTimeout(),
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

    private Subscription subscribeOnUpdateBalanceByTimeout() {
        Observable<Long> obs = Observable.interval(ethereumService.getConfig().getTimeout(), TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io());

        return obs.subscribe(l -> updateBalance(dbService.getWallets()));
    }

    private Subscription subscribeOnUpdateTransactions() {
        EthereumClientConfig config = ethereumService.getConfig();
        Observable<List<Transaction>> obs = ethereumService.getTransactionsObservable()
        	.buffer(config.getBufferTimeout(), TimeUnit.SECONDS, config.getBufferSize())
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

        return obs.subscribe(transactions-> dbService.confirmICOFarmTransactions(transactions, blockFunction));
    }

    private Subscription subscribeOnPendingTransactions() {
        EthereumClientConfig config = ethereumService.getConfig();
        Observable<List<Transaction>> obs = ethereumService.getPendingTransactionsObservable()
                .buffer(config.getBufferTimeout(), TimeUnit.SECONDS, config.getBufferSize())
                .subscribeOn(Schedulers.io());

        return obs.subscribe(transactions -> dbService.saveUnconfirmedICOFarmTransactions(transactions));
    }

    private void updateBalance(List<Wallet> wallets) {
        wallets.forEach(wallet -> {
            try {
                BigInteger balance = ethereumService.requestBalance(wallet.getAddress());
                updateWalletBalance(wallet.getDocument(), balance);
            } catch (Exception e) {
                LOG.error("Can't get balance for address: {}", wallet.getAddress(), e);
            }
        });
    }

    private void updateWalletBalance(ODocument doc, BigInteger balance) {
        if (balance != null) {
            doc.field(Wallet.OPROPERTY_BALANCE, balance);
            DBClosure.sudoSave(doc);
        }
    }
}
