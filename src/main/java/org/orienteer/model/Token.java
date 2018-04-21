package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.orienteer.util.ICOFarmUtils;

import java.math.BigDecimal;
import java.util.Map;

public class Token extends ODocumentWrapper {

    public static final String CLASS_NAME = "Token";

    public static final String OPROPERTY_NAME        = "name";
    public static final String OPROPERTY_SYMBOL      = "symbol";
    public static final String OPROPERTY_DESCRIPTION = "description";
    public static final String OPROPERTY_ADDRESS     = "address";
    public static final String OPROPERTY_ETH_COST    = "ethereumCost";
    public static final String OPROPERTY_GAS_PRICE   = "gasPrice";
    public static final String OPROPERTY_GAS_LIMIT   = "gasLimit";

    public Token() {
        super(CLASS_NAME);
    }

    public Token(ODocument iDocument) {
        super(iDocument);
    }

    public String getName(String locale) {
        return getNames().get(locale);
    }

    public Map<String, String> getNames() {
        return document.field(OPROPERTY_NAME);
    }

    public Token setNames(Map<String, String> names) {
        document.field(OPROPERTY_NAME, names);
        return this;
    }

    public String getSymbol() {
        return document.field(OPROPERTY_SYMBOL);
    }

    public Token setSymbol(String symbol) {
        document.field(OPROPERTY_SYMBOL, symbol);
        return this;
    }

    public String getDescription() {
        return document.field(OPROPERTY_DESCRIPTION);
    }

    public Token setDescription(String description) {
        document.field(OPROPERTY_DESCRIPTION, description);
        return this;
    }

    public String getAddress() {
        return document.field(OPROPERTY_ADDRESS);
    }

    public Token setAddress(String address) {
        document.field(OPROPERTY_ADDRESS, address);
        return this;
    }

    public BigDecimal getEthCost() {
        return document.field(OPROPERTY_ETH_COST);
    }

    public Token setEthCost(BigDecimal cost) {
        document.field(OPROPERTY_ETH_COST, cost);
        return this;
    }

    public BigDecimal getGasPrice() {
        return document.field(OPROPERTY_GAS_PRICE);
    }

    public Token setGasPrice(BigDecimal price) {
        document.field(OPROPERTY_GAS_PRICE, price);
        return this;
    }

    public BigDecimal getGasLimit() {
        return document.field(OPROPERTY_GAS_LIMIT);
    }

    public Token setGasLimit(BigDecimal gasLimit) {
        document.field(OPROPERTY_GAS_LIMIT, gasLimit);
        return this;
    }

    public boolean isEthereumCurrency() {
        return ICOFarmUtils.isEthereumCurrency(this);
    }
}
