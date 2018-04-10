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
    public void init(ODocument config) {
        if (balanceSubscriber == null && transactionSubscriber == null) {
            ethereumService.init(config);
            balanceSubscriber = subscribeOnUpdateBalanceByTimeout();
            transactionSubscriber = subscribeOnUpdateTransactions();
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

    private Subscription subscribeOnUpdateBalanceByTimeout() {
        Observable<Long> obs = Observable.interval(ethereumService.getConfig().getTimeout(), TimeUnit.MINUTES)
                .subscribeOn(Schedulers.io());

        return obs.subscribe(l -> updateBalance(dbService.getWallets()));
    }

    private Subscription subscribeOnUpdateTransactions() {
        Observable<List<Transaction>> obs = ethereumService.getTransactionObservable()
        	.buffer(ethereumService.getConfig().getBufferTimeout(), TimeUnit.SECONDS,ethereumService.getConfig().getBufferSize())
        	.subscribeOn(Schedulers.io());

        return obs.subscribe(transactions-> {
            for (Transaction t : transactions) {
                if (dbService.isICOFarmTransaction(t)) {
                    try {
                        LOG.info("receive transaction: {}", t.getHash());
                        EthBlock ethBlock = ethereumService.requestBlock(t.getBlockNumberRaw());
                        dbService.saveTransaction(t, new Date(1000 * ethBlock.getBlock().getTimestamp().longValue()));
                    } catch (Exception ex) {
                        LOG.error("Can't get transaction block: {}", t, ex);
                    }
                }
            }
        });
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
