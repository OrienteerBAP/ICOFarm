package org.orienteer.service.web3;

import org.orienteer.model.TransferEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.Contract;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import rx.Observable;
import rx.Single;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;
import java.util.*;

public class ICOFarmSmartContract extends Contract implements IICOFarmSmartContract {

    private static final Logger LOG = LoggerFactory.getLogger(ICOFarmSmartContract.class);

    protected ICOFarmSmartContract(String contractAddress, Web3j web3j, Credentials credentials) {
        this(contractAddress, web3j, new RawTransactionManager(web3j, credentials));
    }

    protected ICOFarmSmartContract(String contractAddress, Web3j web3j, TransactionManager transactionManager) {
        super("", contractAddress, web3j, transactionManager, new DefaultGasProvider());
    }

    @Override
    public Single<TransactionReceipt> buy(BigInteger weiAmount) {
        return executeRemoteCallTransaction(createBuyFunction(), weiAmount).observable().first().toSingle();
    }

    @Override
    public Single<TransactionReceipt> sell(BigInteger tokenAmount) {
        return Single.error(new NotImplementedException());
    }

    @Override
    public Single<TransactionReceipt> transfer(String to, BigInteger tokenAmount) {
        return executeRemoteCallTransaction(createTransferFunction(to, tokenAmount)).observable().toSingle();
    }

    @Override
    public Single<BigInteger> estimateGasForBuy(BigInteger weiAmount) {
        return estimateGasCost(createBuyFunction(), weiAmount);
    }

    @Override
    public Single<BigInteger> estimateGasForSell(BigInteger tokenAmount) {
        return Single.just(BigInteger.ZERO);
    }

    @Override
    public Single<BigInteger> estimateGasForTransfer(String to, BigInteger tokenAmount) {
        return estimateGasCost(createTransferFunction(to, tokenAmount));
    }

    @Override
    public Observable<TransferEvent> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        Event event = createTransferEvent();
        EthFilter filter = createTransferEventFilter(event, startBlock, endBlock);

        return web3j.ethLogObservable(filter)
                .flatMap(log -> getTransactionByHash(log.getTransactionHash())
                    .flatMap(transaction -> getBlockByHash(log.getBlockHash())
                        .map(block -> {
                            Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(event, log);
                            return new TransferEvent(
                                    transaction,
                                    block,
                                    (BigInteger) eventValues.getNonIndexedValues().get(0).getValue(),
                                    (String) eventValues.getIndexedValues().get(1).getValue()
                            );
                        })
                    )
        );
    }

    @Override
    public Single<BigInteger> getBalance() {
        Function func = createBalanceOfFunction(transactionManager.getFromAddress());
        return executeRemoteCallSingleValueReturn(func, BigInteger.class).observable().toSingle();
    }

    @Override
    public Single<BigInteger> getBuyPrice() {
        Function func = createBuyPriceFunction();
        return executeRemoteCallSingleValueReturn(func, BigInteger.class).observable().toSingle();
    }

    @Override
    public Single<BigInteger> getSellPrice() {
        Function func = createSellPriceFunction();
        return executeRemoteCallSingleValueReturn(func, BigInteger.class).observable().toSingle();
    }

    @Override
    public Single<BigInteger> getTotalSupply() {
        Function func = createTotalSupplyFunction();
        return executeRemoteCallSingleValueReturn(func, BigInteger.class).observable().toSingle();
    }

    private Function createBuyFunction() {
        return new Function(FUNC_BUY, Collections.emptyList(), Collections.emptyList());
    }

    private Function createTransferFunction(String to, BigInteger tokenAmount) {
        List<Type> args = new ArrayList<>(2);
        args.add(new org.web3j.abi.datatypes.Address(to));
        args.add(new org.web3j.abi.datatypes.generated.Uint256(tokenAmount));
        return new Function(FUNC_TRANSFER, args, Collections.emptyList());
    }

    private Function createBalanceOfFunction(String address) {
        return new Function(
                FUNC_BALANCE_OF,
                Collections.singletonList(new org.web3j.abi.datatypes.Address(address)),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
    }

    private Function createTotalSupplyFunction() {
        return new Function(FUNC_TOTAL_SUPPLY, Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {}));
    }

    private Function createBuyPriceFunction() {
        return new Function(
                FUNC_BUY_PRICE,
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
    }

    private Function createSellPriceFunction() {
        return new Function(
                FUNC_SELL_PRICE,
                Collections.emptyList(),
                Collections.singletonList(new TypeReference<Uint256>() {})
        );
    }

    private Observable<EthBlock.Block> getBlockByHash(String hash) {
        return web3j.ethGetBlockByHash(hash, false)
                .observable()
                .map(EthBlock::getBlock);
    }

    private Observable<Transaction> getTransactionByHash(String hash) {
        return web3j.ethGetTransactionByHash(hash).observable()
                .map(EthTransaction::getTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    private EthFilter createTransferEventFilter(Event event, DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return filter;
    }

    private Event createTransferEvent() {
        return new Event(EVENT_TRANSFER,
                Arrays.asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Collections.singletonList(new TypeReference<Uint256>() {}));
    }

    private Single<BigInteger> estimateGasCost(Function function) {
        return estimateGasCost(function, BigInteger.ZERO);
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
                                .doOnNext(t -> {
                                    if (t.hasError()) {
                                        LOG.error("error: {}", t.getError().getMessage());
                                        LOG.error("code: {}", t.getError().getCode());
                                    }
                                })
                                .map(EthEstimateGas::getAmountUsed).toSingle());
    }

    public static ICOFarmSmartContract load(String contractAddress, Web3j web3j, Credentials credentials) {
        return new ICOFarmSmartContract(contractAddress, web3j, credentials);
    }

    public static ICOFarmSmartContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager) {
        return new ICOFarmSmartContract(contractAddress, web3j, transactionManager);
    }
}
