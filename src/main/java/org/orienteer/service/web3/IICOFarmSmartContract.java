package org.orienteer.service.web3;

import rx.Completable;
import rx.Single;

import java.math.BigInteger;

public interface IICOFarmSmartContract {

    public Completable buy(BigInteger weiAmount);
    public Completable sell(BigInteger tokenAmount);
    public Completable transfer(String to, BigInteger tokenAmount);

    public Single<BigInteger> estimateGasForBuy(BigInteger weiAmount);
    public Single<BigInteger> estimateGasForSell(BigInteger tokenAmount);
    public Single<BigInteger> estimateGasForTransfer(String to, BigInteger tokenAmount);
}
