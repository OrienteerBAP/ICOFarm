package org.orienteer.component.token;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.List;

public class BuyTokenPanel extends AbstractTokenPanel {

    private static final Logger LOG = LoggerFactory.getLogger(BuyTokenPanel.class);

	@Inject
	private IEthereumService service;

	public BuyTokenPanel(String id, IModel<Wallet> wallet, IModel<Token> token) {
		super(id, wallet, token);
	}

    @Override
    @SuppressWarnings("unchecked")
    protected void onInitialize(Form<?> form) {
        TextField<String> field = new TextField<>("tokensQuantity", Model.of());
        field.setOutputMarkupId(true);
        adjustTokensInputFields((TextField<String>) form.get("quantity"), field);
        form.add(field);
    }

    private void adjustTokensInputFields(TextField<String> weiInputField, TextField<String> tokensInputField) {
        weiInputField.add(createEthFieldEventBehavior(weiInputField, tokensInputField));
	    tokensInputField.add(createTokensFieldEventBehavior(weiInputField, tokensInputField));
    }

    private AjaxEventBehavior createEthFieldEventBehavior(TextField<String> weiInputField, TextField<String> tokensInputField) {
	    return new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String weiValue = weiInputField.getModelObject();
                Token token = getTokenModel().getObject();
                if (!Strings.isNullOrEmpty(weiValue) && token != null) {
                    try {
                        BigInteger wei = new BigInteger(weiValue);
                        BigInteger weiCost = Convert.toWei(token.getEthCost(), Convert.Unit.ETHER).toBigInteger();
                        BigInteger tokensQuantity = wei.divide(weiCost);
                        String tokensValue = tokensInputField.getModelObject();

                        if (Strings.isNullOrEmpty(tokensValue) || !tokensQuantity.equals(new BigInteger(tokensValue))) {
                            tokensInputField.setModelObject(tokensQuantity.toString());
                            target.add(tokensInputField);
                        }
                    } catch (NumberFormatException ex) {

                    }
                }
            }
        };
    }

    private AjaxEventBehavior createTokensFieldEventBehavior(TextField<String> weiInputField, TextField<String> tokensInputField) {
	    return new AjaxFormComponentUpdatingBehavior("input") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                String tokensValue = tokensInputField.getModelObject();
                Token token = getTokenModel().getObject();
                if (!Strings.isNullOrEmpty(tokensValue) && token != null) {
                    try {
                        BigInteger tokens = new BigInteger(tokensValue);

                        BigInteger weiCost = Convert.toWei(getTokenModel().getObject().getEthCost(), Convert.Unit.ETHER).toBigInteger();
                        BigInteger weiQuantity = tokens.multiply(weiCost);
                        String weiValue = weiInputField.getModelObject();

                        if (Strings.isNullOrEmpty(weiValue) || !weiQuantity.equals(new BigInteger(weiValue))) {
                            weiInputField.setModelObject(weiQuantity.toString());
                            target.add(weiInputField);
                        }
                    } catch (NumberFormatException ex) {

                    }
                }
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
	    try {
	        String password = ((TextField<String>) form.get("password")).getModelObject();
	        String quantity = ((TextField<String>) form.get("quantity")).getModelObject();
	        buyTokens(password, new BigInteger(quantity));
	        onBuyTokens(target);
        } catch (Exception ex) {
	        LOG.error("Can't buy token(s)!", ex);
	        error(new ResourceModel("buy.token.error").getObject());
        }
    }

    @Override
    protected List<Token> getTokens(IDBService dbService) {
        return dbService.getTokens(false);
    }

    private void buyTokens(String password, BigInteger quantity) throws Exception {
        Token token = getTokenModel().getObject();
        Wallet wallet = getWalletModel().getObject();
        String tokenAddress = token.getAddress();
        Credentials credentials = service.readWallet(password, wallet.getWalletJSON());
        BigInteger gasPrice = token.getGasPrice().toBigInteger();
        BigInteger gasLimit = token.getGasLimit().toBigInteger();
        service.buyTokens(credentials, tokenAddress, quantity, gasPrice, gasLimit);// TODO: add state which displays status of buying tokens
    }

    protected void onBuyTokens(AjaxRequestTarget target) {

    }

    @Override
    protected IModel<String> getTitleModel() {
        return new ResourceModel("buy.token.title");
    }

    @Override
    protected IModel<String> getPasswordLabelModel() {
        return new ResourceModel("buy.token.password");
    }

    @Override
    protected IModel<String> getSelectWalletLabelModel() {
        return new ResourceModel("buy.token.select.wallet");
    }

    @Override
    protected IModel<String> getSelectTokenLabelModel() {
        return new ResourceModel("buy.token.select.token");
    }

    @Override
    protected IModel<String> getQuantityLabelModel() {
        return new ResourceModel("buy.token.eth.quantity");
    }

    @Override
    protected IModel<String> getSubmitBtnLabelModel() {
        return new ResourceModel("buy.token.submit");
    }
}
