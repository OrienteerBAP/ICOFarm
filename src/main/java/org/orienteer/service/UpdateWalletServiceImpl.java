package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EmbeddedWallet;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class UpdateWalletServiceImpl implements IUpdateWalletService {

    @Inject
    private IEthereumService ethereumService;

    @Override
    public void updateBalance(List<EmbeddedWallet> wallets) {
        updateBalance(wallets, null);
    }

    @Override
    public void updateBalance(List<EmbeddedWallet> wallets, Consumer<BigInteger> balanceConsumer) {
        wallets.forEach(wallet -> ethereumService.requestBalanceAsync(wallet.getAddress(), (err, balance) -> {
            if (balance != null) {
                if (balanceConsumer != null) balanceConsumer.accept(balance);
                wallet.setBalance(balance.toString());
                wallet.sudoSave();
            }
        }));
    }

    @Override
    public void updateBalance(EmbeddedWallet wallet) {
        updateBalance(wallet.getDocument());
    }

    @Override
    public void updateBalance(ODocument doc) {
        ethereumService.requestBalanceAsync(doc.field(EmbeddedWallet.OPROPERTY_ADDRESS), (err, balance) -> {
            if (balance != null) {
                doc.field(EmbeddedWallet.OPROPERTY_BALANCE, balance);
                DBClosure.sudoSave(doc);
            }
        });
    }
}
