package org.orienteer.service.web3;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.Token;
import org.orienteer.model.TransferEvent;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Singleton
public class EthereumUpdateServiceImpl implements IEthereumUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumUpdateServiceImpl.class);

    @Inject
    private IEthereumService ethService;

    @Inject
    private IDBService dbService;

    private CompositeSubscription compositeSubscription;

    @Override
    public void init(ODocument config) {
        if (compositeSubscription == null || !compositeSubscription.hasSubscriptions()) {
            if (compositeSubscription == null) compositeSubscription = new CompositeSubscription();
            ethService.init(config);

            compositeSubscription.addAll(
                    subscribeOnPendingTransactions(),
                    subscribeOnUpdateTransactions(),
                    subscribeOnTransferTokenEvents()
            );
            updateTokensPrice();
        }
    }

    @Override
    public void destroy() {
        compositeSubscription.clear();
        ethService.destroy();
    }

    private Subscription subscribeOnUpdateTransactions() {
        EthereumClientConfig config = ethService.getConfig();
        Observable<List<Transaction>> obs = ethService.getTransactionsObservable()
                .distinct()
                .filter(t -> !dbService.isTokenAddress(t.getTo()))
                .buffer(config.getTransactionsBufferDelay(), TimeUnit.SECONDS, config.getTransactionsBufferSize())
                .subscribeOn(Schedulers.io());

        Function<Transaction, EthBlock.Block> blockFunction = transaction -> {
            EthBlock result = null;
            try {
                result = ethService.requestBlock(transaction.getBlockNumberRaw());
            } catch (Exception ex) {
                LOG.error("Can't get transaction block: {}", transaction, ex);
            }
            return result != null ? result.getBlock() : null;
        };

        return obs.subscribe(transactions-> dbService.confirmICOFarmTransactions(transactions, blockFunction),
                (t) -> LOG.error("Can't receive new transactions!", t));
    }

    private Subscription subscribeOnPendingTransactions() {
        EthereumClientConfig config = ethService.getConfig();
        Observable<List<Transaction>> obs = ethService.getPendingTransactionsObservable()
                .distinct()
                .buffer(config.getTransactionsBufferDelay(), TimeUnit.SECONDS, config.getTransactionsBufferSize())
                .subscribeOn(Schedulers.io());

        return obs.subscribe(transactions -> dbService.saveUnconfirmedICOFarmTransactions(transactions),
                (t) -> LOG.error("Can't receive pending transactions!", t));
    }

    private Subscription subscribeOnTransferTokenEvents() {
        EthereumClientConfig config = ethService.getConfig();
        List<Token> tokens = dbService.getTokens(false);

        Observable<List<TransferEvent>> obs = Observable.from(tokens)
                .map(t -> ethService.loadSmartContract(t.getOwner(), t))
                .flatMap(t -> t.transferEventObservable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST))
                .buffer(config.getTransactionsBufferDelay(), TimeUnit.SECONDS, config.getTransactionsBufferSize())
                .subscribeOn(Schedulers.io());

        return obs.subscribe(events -> dbService.saveTransactionsFromTransferEvents(events),
                t -> LOG.error("Can't receive token events!", t));
    }

    private void updateTokensPrice() {
        createUpdatingTokenPrice()
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private Completable createUpdatingTokenPrice() {
        return Observable.from(dbService.getTokens(false))
                .filter(token -> token.getOwner() != null)
                .flatMap(token -> {
                    Wallet owner = token.getOwner();
                    IICOFarmSmartContract contract = ethService.loadSmartContract(owner.getAddress(), token);

                    return contract.getBuyPrice()
                            .map(wei -> Convert.fromWei(new BigDecimal(wei), Convert.Unit.ETHER))
                            .toObservable()
                            .doOnNext(ether -> dbService.save(token.setEtherCost(ether)));
                }).toCompletable();
    }
}
