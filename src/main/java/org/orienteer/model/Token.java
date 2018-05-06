package org.orienteer.model;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.orienteer.util.ICOFarmUtils;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.Map;

public class Token extends ODocumentWrapper {

    public static final String CLASS_NAME = "Token";

    public static final String OPROPERTY_NAME        = "name";
    public static final String OPROPERTY_SYMBOL      = "symbol";
    public static final String OPROPERTY_DESCRIPTION = "description";
    public static final String OPROPERTY_ADDRESS     = "address";
    public static final String OPROPERTY_ETHER_COST  = "etherCost";
    public static final String OPROPERTY_OWNER       = "owner";

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

    public BigDecimal getEtherCost() {
        return document.field(OPROPERTY_ETHER_COST);
    }

    public BigDecimal getEtherCostAs(Convert.Unit unit) {
        if (unit == Convert.Unit.ETHER) {
            return getEtherCost();
        }
        BigDecimal wei = Convert.toWei(getEtherCost(), Convert.Unit.ETHER);
        if (unit == Convert.Unit.WEI) {
            return wei;
        }
        return Convert.fromWei(wei, unit);
    }

    public Token setEtherCost(BigDecimal cost) {
        document.field(OPROPERTY_ETHER_COST, cost);
        return this;
    }

    public Wallet getOwner() {
        ODocument doc = document.field(OPROPERTY_OWNER);
        return doc != null ? new Wallet(doc) : null;
    }

    public Token setOwner(Wallet owner) {
        return setOwner(owner.getDocument());
    }

    public Token setOwner(ODocument owner) {
        document.field(OPROPERTY_OWNER, owner);
        return this;
    }

    public boolean isEthereumCurrency() {
        return ICOFarmUtils.isEthereumCurrency(this);
    }
}
