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

    public String createWallet(String password) throws Exception;
    public void createWalletAsync(String password, BiConsumer<Exception, String> callback);

    public Credentials requestWallet(String password, String fileName) throws Exception;
    public void requestWalletAsync(String password, String fileName, BiConsumer<Exception, Credentials> callback);

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
