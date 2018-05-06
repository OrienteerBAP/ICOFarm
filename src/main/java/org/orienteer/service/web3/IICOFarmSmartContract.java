package org.orienteer.service.web3;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import rx.Single;

import java.math.BigInteger;

public interface IICOFarmSmartContract {

    public Single<TransactionReceipt> buy(BigInteger weiAmount);
    public Single<TransactionReceipt> sell(BigInteger tokenAmount);
    public Single<TransactionReceipt> transfer(String to, BigInteger tokenAmount);

    public Single<BigInteger> getBalance();

    public Single<BigInteger> getBuyPrice();
    public Single<BigInteger> getSellPrice();

    public Single<BigInteger> estimateGasForBuy(BigInteger weiAmount);
    public Single<BigInteger> estimateGasForSell(BigInteger tokenAmount);
    public Single<BigInteger> estimateGasForTransfer(String to, BigInteger tokenAmount);
}
