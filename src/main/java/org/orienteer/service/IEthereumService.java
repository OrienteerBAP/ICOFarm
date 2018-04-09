package org.orienteer.service;

import com.google.inject.ImplementedBy;
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
    public Credentials requestWallet(String password, String fileName) throws Exception;

    public void createWalletAsync(String password, BiConsumer<Exception, String> callback);

    public void createTransaction(String password, String from, String to, BiConsumer<Exception, String> callback);

    public void requestWalletAsync(String password, String fileName, BiConsumer<Exception, Credentials> callback);

    public EthBlock requestBlock(String number) throws Exception;

    public BigInteger requestBalance(String address) throws Exception;

    public void requestBalanceAsync(String address, BiConsumer<Exception, BigInteger> callback);

    public Observable<Transaction> getTransactionObservable();

    public boolean isAddressValid(String address);

    public EthereumClientConfig getConfig();

    public void init();
    public void destroy();
}
