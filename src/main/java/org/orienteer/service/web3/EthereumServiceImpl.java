package org.orienteer.service.web3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.Token;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.Single;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Singleton
public class EthereumServiceImpl implements IEthereumService {

    private Web3j web3j;
    private EthereumClientConfig clientConfig;

    @Override
    public byte[] createWallet(String password) throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        return new ObjectMapper().writeValueAsBytes(Wallet.createStandard(password, ecKeyPair));
    }

    @Override
    public Single<Credentials> readWallet(String password, byte [] data) {
        return Single.fromCallable(() -> new ObjectMapper().readValue(data, WalletFile.class))
                .flatMap(walletFile ->
                        Single.fromCallable(() -> Wallet.decrypt(password, walletFile))
                                .map(Credentials::create)
                );
    }

    @Override
    public EthBlock requestBlock(String number) throws Exception {
        return web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
    }

    @Override
    public Transaction requestTransactionByHash(String hash) throws Exception {
        return web3j.ethGetTransactionByHash(hash).send().getResult();
    }

    @Override
    public Single<TransactionReceipt> transferCurrency(Credentials credentials,
                                                                  String targetAddress,
                                                                  BigDecimal value,
                                                                  Convert.Unit unit) {
        return Single.fromCallable(() -> Transfer.sendFunds(web3j, credentials, targetAddress, value, unit).observable())
                .flatMap(Observable::toSingle);
    }

    @Override
    public Single<BigInteger> requestBalance(String address) {
        return web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .observable().map(EthGetBalance::getBalance).toSingle();
    }

    @Override
    public Single<BigDecimal> requestBalance(String address, Token token) {
        if (token.isEthereumCurrency()) {
            return requestBalance(address).map(wei -> {
                Convert.Unit unit = Convert.Unit.fromString(token.getName("en"));
                return Convert.fromWei(new BigDecimal(wei), unit);
            });
        }
        return loadSmartContract(address, token).getBalance().map(BigDecimal::new);
    }

    @Override
    public void requestBalanceAsync(String address, BiConsumer<Exception, BigInteger> callback) {
        wrapAndRunAsync(callback, () -> {
            EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            if (balance.hasError()) {
                throw new IllegalStateException(balance.getError().getMessage());
            }
            return balance.getBalance();
        });
    }

    @Override
    public Observable<Transaction> getTransactionsObservable() {
        return web3j.transactionObservable();
    }

    @Override
    public Observable<Transaction> getPendingTransactionsObservable() {
        return web3j.pendingTransactionObservable();
    }

    @Override
    public IICOFarmSmartContract loadSmartContract(Credentials credentials, Token token) {
        if (token.isEthereumCurrency()) {
            throw new IllegalStateException("Can't load contract from Ethereum currency!");
        }
        return ICOFarmSmartContract.load(token.getAddress(), web3j, credentials);
    }

    @Override
    public IICOFarmSmartContract loadSmartContract(String from, Token token) {
        if (token.isEthereumCurrency()) {
            throw new IllegalStateException("Can't load contract from Ethereum currency!");
        }
        return ICOFarmSmartContract.load(token.getAddress(), web3j, new ClientTransactionManager(web3j, from));
    }

    @Override
    public EthereumClientConfig getConfig() {
        return clientConfig;
    }

    @Override
    public void init(ODocument config) {
        clientConfig = new EthereumClientConfig(config);
        web3j = Web3j.build(new HttpService(clientConfig.getHost() + ":" + clientConfig.getPort()));
    }

    @Override
    public void destroy() {
        web3j = null;
        clientConfig = null;
    }

    @Override
    public boolean isAddressValid(String address) {
        return !Strings.isNullOrEmpty(address) && WalletUtils.isValidAddress(address);
    }

    @Override
    public Single<BigInteger> getGasPrice() {
        return web3j.ethGasPrice().observable().map(EthGasPrice::getGasPrice).toSingle();
    }

    private <T> void wrapAndRunAsync(BiConsumer<Exception, T> callback, Callable<T> callable) {
        CompletableFuture.runAsync(() -> {
            try {
                callback.accept(null, callable.call());
            } catch (Exception e) {
                callback.accept(e, null);
            }
        });
    }
}
