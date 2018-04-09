package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class EthereumUpdateServiceImpl implements IEthereumUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(EthereumUpdateServiceImpl.class);

    @Inject
    private IEthereumService ethereumService;

    @Inject
    private IDbService dbService;

    private Subscription balanceSubscriber;
    private Subscription transactionSubscriber;


    @Override
    public void init() {
        if (balanceSubscriber == null && transactionSubscriber == null) {
            ethereumService.init();
            balanceSubscriber = updateBalanceByTimeout().subscribe();
            transactionSubscriber = updateTransactions().subscribe();
        }
    }

    @Override
    public void destroy() {
        balanceSubscriber.unsubscribe();
        transactionSubscriber.unsubscribe();
        ethereumService.destroy();
        balanceSubscriber = null;
        transactionSubscriber = null;
    }

    private Observable<Long> updateBalanceByTimeout() {
        return Observable.interval(ethereumService.getConfig().getTimeout(), TimeUnit.MINUTES)
                .doOnNext(l -> updateBalance(dbService.getWallets()))
                .subscribeOn(Schedulers.io());
    }

    private Observable<Transaction> updateTransactions() {
        return ethereumService.getTransactionObservable()
                .filter(dbService::isICOFarmTransaction)
                .doOnNext(t -> {
                    try {
                        LOG.info("receive transaction: {}", t.getHash());
                        EthBlock ethBlock = ethereumService.requestBlock(t.getBlockNumberRaw());
                        dbService.saveTransaction(t, new Date(1000 * ethBlock.getBlock().getTimestamp().longValue()));
                    } catch (Exception ex) {
                        LOG.error("Can't get transaction block: {}", t, ex);
                    }
                }).subscribeOn(Schedulers.io());
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
