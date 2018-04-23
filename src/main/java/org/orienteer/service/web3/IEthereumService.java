package org.orienteer.service.web3;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.Token;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import rx.Observable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@ImplementedBy(EthereumServiceImpl.class)
public interface IEthereumService {

    public byte[] createWallet(String password) throws Exception;

    public Credentials readWallet(String password, byte[] data) throws Exception;

    public BigInteger requestBalance(String address) throws Exception;
    public void requestBalanceAsync(String address, BiConsumer<Exception, BigInteger> callback);

    public EthBlock requestBlock(String number) throws Exception;

    public Transaction requestTransactionByHash(String hash) throws Exception;

    public CompletableFuture<TransactionReceipt> buyTokens(Credentials credentials,
                                                           String contractAddress,
                                                           BigInteger ethQuantity,
                                                           BigInteger gasPrice,
                                                           BigInteger gasLimit);

    public CompletableFuture<TransactionReceipt> transferTokens(Credentials credentials,
                                                                String contractAddress,
                                                                String targetAddress,
                                                                BigInteger quantity,
                                                                BigInteger gasPrice,
                                                                BigInteger gasLimit);

    public CompletableFuture<TransactionReceipt> transferCurrency(Credentials credentials,
                                                                  String targetAddress,
                                                                  BigDecimal value,
                                                                  Convert.Unit unit) throws Exception;

    public Observable<BigInteger> requestBalance(String address, Token token);

    public Observable<Transaction> getTransactionsObservable();

    public Observable<Transaction> getPendingTransactionsObservable();

    public boolean isAddressValid(String address);

    public EthereumClientConfig getConfig();

    public void init(ODocument config);
    public void destroy();
}
