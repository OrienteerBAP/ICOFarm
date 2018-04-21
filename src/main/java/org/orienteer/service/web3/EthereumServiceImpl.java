package org.orienteer.service.web3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.Token;
import org.orienteer.util.ICOFarmUtils;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import rx.Observable;
import rx.Single;

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
    public Credentials readWallet(String password, byte [] data) throws Exception {
        WalletFile walletFile = new ObjectMapper().readValue(data, WalletFile.class);
        return Credentials.create(Wallet.decrypt(password, walletFile));
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
    public CompletableFuture<TransactionReceipt> buyTokens(Credentials credentials,
                                                           String contractAddress,
                                                           BigInteger ethQuantity,
                                                           BigInteger gasPrice,
                                                           BigInteger gasLimit) {
        Buyable token = Buyable.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        return token.buy(ethQuantity).sendAsync();
    }

    @Override
    public CompletableFuture<TransactionReceipt> transferTokens(Credentials credentials,
                                                                String contractAddress,
                                                                String targetAddress,
                                                                BigInteger ethQuantity,
                                                                BigInteger gasPrice,
                                                                BigInteger gasLimit) {
        ERC20Interface token = ERC20Interface.load(contractAddress, web3j, credentials, gasPrice, gasLimit);
        return token.transfer(targetAddress, ethQuantity).sendAsync();
    }

    @Override
    public Observable<BigInteger> requestBalance(String address, Token token) {
        if (!ICOFarmUtils.isEthereumCurrency(token)) {
            ERC20Interface erc20 = ERC20Interface.load(token.getAddress(), web3j, new ReadonlyTransactionManager(web3j, address),
                    token.getGasPrice().toBigInteger(), token.getGasLimit().toBigInteger());
            return erc20.balanceOf(address).observable();
        }

        return requestEthereumBalance(address);
    }

    @Override
    public BigInteger requestBalance(String address) throws Exception {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return balance.getBalance();
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

    private <T> void wrapAndRunAsync(BiConsumer<Exception, T> callback, Callable<T> callable) {
        CompletableFuture.runAsync(() -> {
            try {
                callback.accept(null, callable.call());
            } catch (Exception e) {
                callback.accept(e, null);
            }
        });
    }

    private Observable<BigInteger> requestEthereumBalance(String address) {
        return Single.fromCallable(() -> {
            try {
                return requestBalance(address);
            } catch (Exception ex) {
                return BigInteger.valueOf(0);
            }
        }).toObservable();
    }
}
