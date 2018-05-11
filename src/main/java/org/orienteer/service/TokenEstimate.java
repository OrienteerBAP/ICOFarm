package org.orienteer.service;

import org.orienteer.model.Token;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.MathContext;

public class TokenEstimate implements ITokenEstimate {


    @Override
    public BigDecimal estimateTokens(String value, Token currency, Token token) {
        BigDecimal weiCost = getWeiCost(token);
        Convert.Unit unit = Convert.Unit.fromString(currency.getName("en"));
        BigDecimal wei = unit == Convert.Unit.WEI ? new BigDecimal(value) : null;
        if (wei == null) {
            wei = Convert.toWei(value, unit);
        }
        return wei.divide(weiCost, BigDecimal.ROUND_CEILING);
    }

    @Override
    public BigDecimal estimateEther(String value, Token currency, Token token) {
        BigDecimal tokens = new BigDecimal(value);
        BigDecimal wei = tokens.multiply(getWeiCost(token), MathContext.UNLIMITED);
        return Convert.fromWei(wei, Convert.Unit.fromString(currency.getName("en")));
    }

    private BigDecimal getWeiCost(Token token) {
        BigDecimal etherCost = token.getEtherCost();
        return Convert.toWei(etherCost, Convert.Unit.ETHER);
    }
}
