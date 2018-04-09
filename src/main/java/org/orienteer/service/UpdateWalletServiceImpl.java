package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class UpdateWalletServiceImpl implements IUpdateWalletService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateWalletServiceImpl.class);

    @Inject
    private IEthereumService ethereumService;

    @Override
    public void update(List<Wallet> wallets) {
        updateBalance(wallets);
        updateTransactions(wallets);
    }

    @Override
    public void updateTransactions(List<Wallet> wallets) {

    }

    @Override
    public void updateBalance(List<Wallet> wallets) {
        updateBalance(wallets, null);
    }

    @Override
    public void updateBalance(List<Wallet> wallets, Consumer<BigInteger> balanceConsumer) {
        wallets.forEach(wallet -> {
            try {
                BigInteger balance = ethereumService.requestBalance(wallet.getAddress());
                updateWalletBalance(wallet.getDocument(), balance);
            } catch (Exception e) {
                LOG.error("Can't get balance for address: {}", wallet.getAddress(), e);
            }
        });
    }

    @Override
    public void updateBalance(Wallet wallet) {
        updateBalance(wallet.getDocument());
    }

    @Override
    public void updateBalance(ODocument doc) {
        try {
            BigInteger balance = ethereumService.requestBalance(doc.field(Wallet.OPROPERTY_ADDRESS));
            updateWalletBalance(doc, balance);
        } catch (Exception e) {
            LOG.error("Can't get balance for address: {}", doc.field(Wallet.OPROPERTY_ADDRESS), e);
        }
    }

    private void updateWalletBalance(ODocument doc, BigInteger balance) {
        if (balance != null) {
            doc.field(Wallet.OPROPERTY_BALANCE, balance);
            DBClosure.sudoSave(doc);
        }
    }
}
