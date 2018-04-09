package org.orienteer.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.orienteer.model.EthereumClientConfig;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.File;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Singleton
public class EthereumServiceImpl implements IEthereumService {

    @Inject
    private Web3j web3j;

    @Inject
    private EthereumClientConfig clientConfig;

    @Override
    public String createWallet(String password) throws Exception {
        return WalletUtils.generateFullNewWalletFile(password, new File(clientConfig.getWorkFolder()));
    }

    @Override
    public Credentials requestWallet(String password, String fileName) throws Exception {
        return WalletUtils.loadCredentials(password, clientConfig.getWorkFolder() + fileName);
    }

    @Override
    public void createWalletAsync(String password, BiConsumer<Exception, String> callback) {
        wrapAndRunAsync(callback, () -> createWallet(password));
    }

    @Override
    public void createTransaction(String password, String from, String to, BiConsumer<Exception, String> callback) {

    }

    @Override
    public void requestWalletAsync(String password, String fileName, BiConsumer<Exception, Credentials> callback) {
        wrapAndRunAsync(callback, () -> requestWallet(password, fileName));
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
