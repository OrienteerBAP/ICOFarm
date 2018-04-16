package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import rx.Observable;

import java.math.BigInteger;
import java.util.function.BiConsumer;

@ImplementedBy(EthereumServiceImpl.class)
public interface IEthereumService {

    public byte[] createWallet(String password) throws Exception;

    public Credentials readWallet(String password, byte[] data) throws Exception;

    public BigInteger requestBalance(String address) throws Exception;
    public void requestBalanceAsync(String address, BiConsumer<Exception, BigInteger> callback);

    public EthBlock requestBlock(String number) throws Exception;

    public Transaction requestTransactionByHash(String hash) throws Exception;

    public Observable<Transaction> getTransactionsObservable();

    public Observable<Transaction> getPendingTransactionsObservable();

    public boolean isAddressValid(String address);

    public EthereumClientConfig getConfig();

    public void init(ODocument config);
    public void destroy();
}
