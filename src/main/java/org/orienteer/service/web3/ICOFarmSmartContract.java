package org.orienteer.service.web3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import rx.Completable;
import rx.Single;

import java.math.BigInteger;
import java.util.Collections;

public class ICOFarmSmartContract extends Contract implements IICOFarmSmartContract {

    private static final Logger LOG = LoggerFactory.getLogger(ICOFarmSmartContract.class);

    public static final String FUNC_BUY      = "buy";
    public static final String FUNC_SELL     = "sell";
    public static final String FUNC_TRANSFER = "transfer";


    protected ICOFarmSmartContract(String contractAddress, Web3j web3j, Credentials credentials) {
        this(contractAddress, web3j, new RawTransactionManager(web3j, credentials));
    }

    protected ICOFarmSmartContract(String contractAddress, Web3j web3j, TransactionManager transactionManager) {
        super("", contractAddress, web3j, transactionManager, new DefaultGasProvider());
    }

    @Override
    public Completable buy(BigInteger weiAmount) {
        return executeRemoteCallTransaction(getBuyFunction(), weiAmount).observable().toCompletable();
    }

    @Override
    public Completable sell(BigInteger tokenAmount) {
        return null;
    }

    @Override
    public Completable transfer(String to, BigInteger tokenAmount) {
        return null;
    }

    @Override
    public Single<BigInteger> estimateGasForBuy(BigInteger weiAmount) {
        return estimateGasCost(getBuyFunction(), weiAmount);
    }

    @Override
    public Single<BigInteger> estimateGasForSell(BigInteger tokenAmount) {
        return null;
    }

    @Override
    public Single<BigInteger> estimateGasForTransfer(String to, BigInteger tokenAmount) {
        return null;
    }

    private Function getBuyFunction() {
        return new Function(FUNC_BUY, Collections.emptyList(), Collections.emptyList());
    }

    private Single<BigInteger> estimateGasCost(Function function, BigInteger value) {
        String encoded = FunctionEncoder.encode(function);
        BigInteger gasPrice = gasProvider.getGasPrice(function.getName());
        BigInteger gasLimit = gasProvider.getGasLimit(function.getName());

        return web3j.ethGetTransactionCount(transactionManager.getFromAddress(), DefaultBlockParameterName.PENDING)
                .observable().toSingle()
                .map(EthGetTransactionCount::getTransactionCount)
                .map(nonce ->
                        org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                                transactionManager.getFromAddress(),
                                nonce,
                                gasPrice,
                                gasLimit,
                                contractAddress,
                                value,
                                encoded))
                .flatMap(transaction ->
                        web3j.ethEstimateGas(transaction)
                                .observable()
                                .map(EthEstimateGas::getAmountUsed).toSingle());
    }

    public static ICOFarmSmartContract load(String contractAddress, Web3j web3j, Credentials credentials) {
        return new ICOFarmSmartContract(contractAddress, web3j, credentials);
    }

    public static ICOFarmSmartContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager) {
        return new ICOFarmSmartContract(contractAddress, web3j, transactionManager);
    }
}
