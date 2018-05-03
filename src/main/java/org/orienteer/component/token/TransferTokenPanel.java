package org.orienteer.component.token;

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
import org.orienteer.module.ICOFarmModule;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class TransferTokenPanel extends AbstractTokenPanel {

	private static final Logger LOG = LoggerFactory.getLogger(TransferTokenPanel.class);

	@Inject
	private IEthereumService service;

	public TransferTokenPanel(String id, IModel<Wallet> walletModel, IModel<Token> tokenModel) {
		super(id, walletModel, tokenModel);
	}

	@Override
	protected void onInitialize(Form<?> form) {
		TextField<String> tokenField = new TextField<>("token", Model.of());
		tokenField.setOutputMarkupId(true);
	    ChooseWalletAddressPanel panel = new ChooseWalletAddressPanel("chooseWalletPanel", Model.of());
	    panel.setRequired(true);
	    form.add(tokenField);
		form.add(new Label("targetLabel", new ResourceModel("transfer.token.target.wallet")));
		form.add(new Label("tokenLabel", new ResourceModel("transfer.token.quantity")));
		form.add(panel);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
		try {
			String password = ((TextField<String>) form.get("password")).getModelObject();
			String targetAddress = ((ChooseWalletAddressPanel) form.get("chooseWalletPanel")).getModelObject();
			String quantity = ((TextField<String>) form.get("token")).getModelObject();
			transferTokens(password, new BigDecimal(quantity), targetAddress);

			onTransferTokens(target);
		} catch (Exception ex) {
			LOG.error("Can't transfer token(s)!", ex);
			error(new ResourceModel("transfer.token.error").getObject() + "\n" + ex.getMessage());
		}
	}

	@Override
	protected List<Token> getTokens(IDBService dbService) {
		return dbService.getTokens(true);
	}

	private void transferTokens(String password, BigDecimal quantity, String target) throws Exception {
		Token token = getTokenModel().getObject();
		Wallet wallet = getWalletModel().getObject();
		Credentials credentials = service.readWallet(password, wallet.getWalletJSON());
		String address = token.getAddress();
		if (!address.equals(ICOFarmModule.ZERO_ADDRESS)) {
			BigInteger gasPrice = token.getGasPrice().toBigInteger();
			BigInteger gasLimit = token.getGasLimit().toBigInteger();
			service.transferTokens(credentials, token.getAddress(), target, quantity.toBigInteger(), gasPrice, gasLimit);
		} else {
			service.transferCurrency(credentials, target, quantity, Convert.Unit.fromString(token.getName("en")));
		}
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
	protected IModel<String> getSubmitBtnLabelModel() {
		return new ResourceModel("transfer.token.submit");
	}
}
