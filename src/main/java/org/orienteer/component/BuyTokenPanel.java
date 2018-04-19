package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class BuyTokenPanel extends AbstractTokenPanel {

	@Inject
	private IEthereumService service;

	public BuyTokenPanel(String id, IModel<Wallet> wallet, IModel<Token> token) {
		super(id, wallet, token);
	}

    @Override
    @SuppressWarnings("unchecked")
    protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
	    try {
	        String password = ((TextField<String>) form.get("password")).getModelObject();
	        long quantity = ((TextField<Long>) form.get("quantity")).getModelObject();
	        buyTokens(password, quantity);
	        onBuyTokens(target);
        } catch (Exception ex) {
	        error(new ResourceModel("buy.token.error").getObject());
        }
    }

    private void buyTokens(String password, long quantity) throws Exception {
        Token token = getTokenModel().getObject();
        Wallet wallet = getWalletModel().getObject();
        String tokenAddress = token.getAddress();
        Credentials credentials = service.readWallet(password, wallet.getWalletJSON());
        BigInteger gasPrice = token.getGasPrice().toBigInteger();
        BigInteger gasLimit = token.getGasLimit().toBigInteger();
        service.buyTokens(credentials, tokenAddress, BigInteger.valueOf(quantity), gasPrice, gasLimit); // TODO: add state which displays status of buying tokens
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
        return new ResourceModel("buy.token.quantity");
    }

    @Override
    protected IModel<String> getSubmitBtnLabelModel() {
        return new ResourceModel("buy.token.submit");
    }
}
