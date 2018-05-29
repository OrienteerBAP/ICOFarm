package org.orienteer.util;

import com.google.common.base.Strings;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.service.web3.IEthereumService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TransferTokensTransactionValidator implements IValidator<String> {

    private final IModel<Wallet> walletModel;
    private final IModel<String> targetModel;
    private final IModel<Token> tokenModel;

    public TransferTokensTransactionValidator(IModel<Wallet> walletModel, IModel<String> targetModel, IModel<Token> tokenModel) {
        this.walletModel = walletModel;
        this.targetModel = targetModel;
        this.tokenModel = tokenModel;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        if (walletModel.getObject() != null && targetModel.getObject() != null && tokenModel.getObject() != null) {
            BigDecimal value = getValueFromString(validatable.getValue());
            if (value != null) {
                validate(value, tokenModel.getObject(), validatable);
            }
        }
    }

    private void validate(BigDecimal value, Token token, IValidatable<String> validatable) {
        IEthereumService ethService = OrienteerWebApplication.lookupApplication().getServiceInstance(IEthereumService.class);
        BigDecimal walletTokens = walletModel.getObject().getBalance(token.getSymbol());
        BigDecimal delta = walletTokens.subtract(value);
        if (delta.compareTo(BigDecimal.ZERO) >= 0) {
            boolean valid = token.isEthereumCurrency() ? isCurrencyTransferValid(delta, ethService) : isTokenTransferValid(walletTokens, ethService);
            if (!valid) {
                error(validatable, "validator.transaction.transfer.gas.not.enough.money");
            }
        } else error(validatable, "validator.transaction.transfer.not.enough.money");
    }

    private boolean isCurrencyTransferValid(BigDecimal delta, IEthereumService ethService) {
        Convert.Unit unit = Convert.Unit.fromString(tokenModel.getObject().getName("en"));
        BigInteger deltaWei = unit != Convert.Unit.WEI ? Convert.toWei(delta, unit).toBigInteger() : delta.toBigInteger();
        BigInteger weiNeed = getGasPrice(ethService).multiply(Transfer.GAS_LIMIT);

        return deltaWei.subtract(weiNeed).compareTo(BigInteger.ZERO) >= 0;
    }

    private boolean isTokenTransferValid(BigDecimal walletTokens, IEthereumService ethService) {
        BigInteger walletWei = walletModel.getObject().getBalance(ICOFarmModule.WEI).toBigInteger();
        BigInteger gas = getGasForTransfer(ethService, walletTokens.toBigInteger());
        BigInteger gasInWei = getGasPrice(ethService).multiply(gas);

        return walletWei.subtract(gasInWei).compareTo(BigInteger.ZERO) >= 0;
    }

    private BigDecimal getValueFromString(String value) {
        BigDecimal result = null;
        if (!Strings.isNullOrEmpty(value)) {
            try {
                result = new BigDecimal(value);
            } catch (NumberFormatException ex) {}
        }
        return result;
    }

    private BigInteger getGasForTransfer(IEthereumService ethService, BigInteger weiAmount) {
        return ethService.loadSmartContract(walletModel.getObject().getAddress(), tokenModel.getObject())
                .estimateGasForTransfer(targetModel.getObject(), weiAmount).toBlocking().value();
    }

    private BigInteger getGasPrice(IEthereumService ethService) {
        return ethService.getGasPrice().toBlocking().value();
    }

    private void error(IValidatable<String> validatable, String key) {
        ValidationError err = new ValidationError();
        err.setMessage(new ResourceModel(key).getObject());
        validatable.error(err);
    }
}
