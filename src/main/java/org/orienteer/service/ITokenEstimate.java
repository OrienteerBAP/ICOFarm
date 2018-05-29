package org.orienteer.service;

import com.google.inject.ImplementedBy;
import org.orienteer.model.Token;

import java.math.BigDecimal;

@ImplementedBy(TokenEstimate.class)
public interface ITokenEstimate {
    /**
     * Estimate token value which user can receive from Ether value
     * @param value
     * @param currency
     * @param token
     * @return
     */
    public BigDecimal estimateTokens(String value, Token currency, Token token) throws NumberFormatException;

    /**
     * Estimate Ether value which need for receive value tokens
     * @param value
     * @param token
     * @param currency
     * @return
     */
    public BigDecimal estimateEther(String value, Token currency, Token token) throws NumberFormatException;
}
