package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.Wallet;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class UpdateWalletServiceImpl implements IUpdateWalletService {

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
        wallets.forEach(wallet -> ethereumService.requestBalanceAsync(wallet.getAddress(), (err, balance) -> {
            if (balance != null) {
                if (balanceConsumer != null) balanceConsumer.accept(balance);
                wallet.setBalance(balance.toString());
                wallet.sudoSave();
            }
        }));
    }

    @Override
    public void updateBalance(Wallet wallet) {
        updateBalance(wallet.getDocument());
    }

    @Override
    public void updateBalance(ODocument doc) {
        ethereumService.requestBalanceAsync(doc.field(Wallet.OPROPERTY_ADDRESS), (err, balance) -> {
            if (balance != null) {
                doc.field(Wallet.OPROPERTY_BALANCE, balance);
                DBClosure.sudoSave(doc);
            }
        });
    }
}
