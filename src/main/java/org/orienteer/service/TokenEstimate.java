package org.orienteer.service;

import org.orienteer.model.Token;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.MathContext;

public class TokenEstimate implements ITokenEstimate {


    @Override
    public BigDecimal estimateTokens(String value, Token currency, Token token) {
        BigDecimal wei = Convert.toWei(value, Convert.Unit.fromString(currency.getName("en")));
        return wei.divide(token.getEtherCost(), BigDecimal.ROUND_CEILING);
    }

    @Override
    public BigDecimal estimateEther(String value, Token currency, Token token) {
        BigDecimal tokens = new BigDecimal(value);
        BigDecimal wei = tokens.multiply(token.getEtherCost(), MathContext.UNLIMITED);
        return Convert.fromWei(wei, Convert.Unit.fromString(currency.getName("en")));
    }
}
