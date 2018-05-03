package org.orienteer.service;

import org.orienteer.model.Token;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.MathContext;

public class TokenEstimate implements ITokenEstimate {


    @Override
    public BigDecimal estimateTokens(String value, Token currency, Token token) {
        BigDecimal wei = Convert.toWei(value, Convert.Unit.fromString(currency.getName("en")));
        BigDecimal weiCost = Convert.toWei(token.getEthCost(), Convert.Unit.ETHER);
        return wei.divide(weiCost, BigDecimal.ROUND_CEILING);
    }

    @Override
    public BigDecimal estimateEther(String value, Token currency, Token token) {
        BigDecimal tokens = new BigDecimal(value);
        BigDecimal weiCost = Convert.toWei(token.getEthCost(), Convert.Unit.ETHER);
        BigDecimal wei = tokens.multiply(weiCost, MathContext.UNLIMITED);
        return Convert.fromWei(wei, Convert.Unit.fromString(currency.getName("en")));
    }
}
