package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EmbeddedWallet;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@ImplementedBy(UpdateWalletServiceImpl.class)
public interface IUpdateWalletService {

    public void updateBalance(List<EmbeddedWallet> wallets);

    public void updateBalance(List<EmbeddedWallet> wallets, Consumer<BigInteger> balanceConsumer);

    public void updateBalance(EmbeddedWallet wallet);

    public void updateBalance(ODocument doc);
}
