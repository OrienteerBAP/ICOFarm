package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.orienteer.model.EmbeddedOWallet;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@Singleton
public class UpdateServiceImpl implements IUpdateService {

    @Inject
    private IEthereumService ethereumService;

    @Override
    public void updateBalance(List<EmbeddedOWallet> wallets) {
        updateBalance(wallets, null);
    }

    @Override
    public void updateBalance(List<EmbeddedOWallet> wallets, Consumer<BigInteger> balanceConsumer) {
        wallets.forEach(wallet -> ethereumService.requestBalanceAsync(wallet.getAddress(), (err, balance) -> {
            if (balance != null) {
                if (balanceConsumer != null) balanceConsumer.accept(balance);
                wallet.setBalance(balance.toString());
                wallet.sudoSave();
            }
        }));
    }
}
