package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EmbeddedOWallet;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@ImplementedBy(UpdateWalletServiceImpl.class)
public interface IUpdateWalletService {

    public void updateBalance(List<EmbeddedOWallet> wallets);

    public void updateBalance(List<EmbeddedOWallet> wallets, Consumer<BigInteger> balanceConsumer);

    public void updateBalance(EmbeddedOWallet wallet);

    public void updateBalance(ODocument doc);
}
