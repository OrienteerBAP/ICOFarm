package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class TransferTokenPanel extends AbstractTokenPanel {

	private static final Logger LOG = LoggerFactory.getLogger(TransferTokenPanel.class);

	@Inject
	private IEthereumService service;

	public TransferTokenPanel(String id, IModel<Wallet> walletModel, IModel<Token> tokenModel) {
		super(id, walletModel, tokenModel);
	}

	@Override
	protected void onInitialize(Form<?> form) {
		TextField<String> target = new TextField<>("target", Model.of());
		target.setRequired(true);
		form.add(new Label("targetLabel", new ResourceModel("transfer.token.target.wallet")));
		form.add(target);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
		try {
			String password = ((TextField<String>) form.get("password")).getModelObject();
			String targetAddress = ((TextField<String>) form.get("target")).getModelObject();
			int quantity = ((TextField<Integer>) form.get("quantity")).getModelObject();
			transferTokens(password, quantity, targetAddress);

			onTransferTokens(target);
		} catch (Exception ex) {
			LOG.error("Can't transfer token(s)!", ex);
			error(new ResourceModel("transfer.token.error").getObject());
		}
	}

	private void transferTokens(String password, int quantity, String target) throws Exception {
		Token token = getTokenModel().getObject();
		Wallet wallet = getWalletModel().getObject();
		Credentials credentials = service.readWallet(password, wallet.getWalletJSON());
		BigInteger gasPrice = token.getGasPrice().toBigInteger();
		BigInteger gasLimit = token.getGasLimit().toBigInteger();
		service.transferTokens(credentials, token.getAddress(), target, BigInteger.valueOf(quantity), gasPrice, gasLimit);
	}

	protected void onTransferTokens(AjaxRequestTarget target) {

	}

	@Override
	protected IModel<String> getTitleModel() {
		return new ResourceModel("transfer.token.title");
	}

	@Override
	protected IModel<String> getPasswordLabelModel() {
		return new ResourceModel("transfer.token.password");
	}

	@Override
	protected IModel<String> getSelectWalletLabelModel() {
		return new ResourceModel("transfer.token.select.wallet");
	}

	@Override
	protected IModel<String> getSelectTokenLabelModel() {
		return new ResourceModel("transfer.token.select.token");
	}

	@Override
	protected IModel<String> getQuantityLabelModel() {
		return new ResourceModel("transfer.token.quantity");
	}

	@Override
	protected IModel<String> getSubmitBtnLabelModel() {
		return new ResourceModel("transfer.token.submit");
	}
}
