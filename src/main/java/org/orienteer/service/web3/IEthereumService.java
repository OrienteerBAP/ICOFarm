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
import rx.Single;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.BiConsumer;

@ImplementedBy(EthereumServiceImpl.class)
public interface IEthereumService {

    public byte[] createWallet(String password) throws Exception;

    public Single<Credentials> readWallet(String password, byte[] data);

    public Single<BigInteger> requestBalance(String address);
    public Single<BigDecimal> requestBalance(String address, Token token);

    public void requestBalanceAsync(String address, BiConsumer<Exception, BigInteger> callback);

    public EthBlock requestBlock(String number) throws Exception;

    public Transaction requestTransactionByHash(String hash) throws Exception;

    public Single<TransactionReceipt> transferCurrency(Credentials credentials,
                                                                  String targetAddress,
                                                                  BigDecimal value,
                                                                  Convert.Unit unit);

    public Observable<Transaction> getTransactionsObservable();

    public Observable<Transaction> getPendingTransactionsObservable();

    public IICOFarmSmartContract loadSmartContract(Credentials credentials, Token token);
    public IICOFarmSmartContract loadSmartContract(String from, Token token);

    public boolean isAddressValid(String address);

    public EthereumClientConfig getConfig();

    public void init(ODocument config);
    public void destroy();
}
