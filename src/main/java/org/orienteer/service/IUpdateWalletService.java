package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.Wallet;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Consumer;

@ImplementedBy(UpdateWalletServiceImpl.class)
public interface IUpdateWalletService {

    public void update(List<Wallet> wallets);

    public void updateTransactions(List<Wallet> wallets);

    public void updateBalance(List<Wallet> wallets);
    public void updateBalance(List<Wallet> wallets, Consumer<BigInteger> balanceConsumer);
    public void updateBalance(Wallet wallet);
    public void updateBalance(ODocument doc);
}
