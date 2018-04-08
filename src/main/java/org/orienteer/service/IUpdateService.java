package org.orienteer.service;

import com.google.inject.ImplementedBy;
import org.orienteer.model.EmbeddedOWallet;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@ImplementedBy(UpdateServiceImpl.class)
public interface IUpdateService {

    public void updateBalance(List<EmbeddedOWallet> wallets);

    public void updateBalance(List<EmbeddedOWallet> wallets, Consumer<BigInteger> balanceConsumer);
}
