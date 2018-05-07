package org.orienteer.service.web3;

import org.orienteer.model.TransferEvent;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import rx.Observable;
import rx.Single;

import java.math.BigInteger;

public interface IICOFarmSmartContract {

    public static final String FUNC_BUY        = "buy";
    public static final String FUNC_SELL       = "sell";
    public static final String FUNC_TRANSFER   = "transfer";
    public static final String FUNC_BALANCE_OF = "balanceOf";
    public static final String FUNC_BUY_PRICE  = "buyPrice";
    public static final String FUNC_SELL_PRICE = "sellPrice";

    public static final String EVENT_TRANSFER  = "Transfer";

    public Single<TransactionReceipt> buy(BigInteger weiAmount);
    public Single<TransactionReceipt> sell(BigInteger tokenAmount);
    public Single<TransactionReceipt> transfer(String to, BigInteger tokenAmount);

    public Single<BigInteger> getBalance();

    public Single<BigInteger> getBuyPrice();
    public Single<BigInteger> getSellPrice();

    public Single<BigInteger> estimateGasForBuy(BigInteger weiAmount);
    public Single<BigInteger> estimateGasForSell(BigInteger tokenAmount);
    public Single<BigInteger> estimateGasForTransfer(String to, BigInteger tokenAmount);

    public Observable<TransferEvent> transferEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock);
}
