package org.orienteer.component.token;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.util.TransferTokensTransactionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import rx.Single;
import rx.schedulers.Schedulers;

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
	    ChooseWalletAddressPanel panel = new ChooseWalletAddressPanel("chooseWalletPanel", Model.of());
	    panel.setRequired(true);

		TextField<String> tokenField = new RequiredTextField<>("token", Model.of());
		tokenField.setOutputMarkupId(true);
		tokenField.add(new TransferTokensTransactionValidator(getWalletModel(), panel.getModel(), getTokenModel()));

		form.add(panel);
		form.add(tokenField);
		form.add(new Label("targetLabel", new ResourceModel("transfer.token.target.wallet")));
		form.add(new Label("tokenLabel", new ResourceModel("transfer.token.quantity")));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onFormSubmit(AjaxRequestTarget target, Form<?> form) {
        String password = ((TextField<String>) form.get("password")).getModelObject();
        String targetAddress = ((ChooseWalletAddressPanel) form.get("chooseWalletPanel")).getModelObject();
        String quantity = ((TextField<String>) form.get("token")).getModelObject();
		Credentials credentials = readWallet(password);

		if (credentials != null) {
			transferTokens(credentials, targetAddress, quantity)
					.subscribeOn(Schedulers.io())
					.doOnSubscribe(() -> onTransferTokens(target))
					.subscribe(
							(tr) -> {},
							(err) -> LOG.error("Can't buy tokens!", err)
					);
		} else error(new ResourceModel("transfer.token.wrong.password").getObject());
	}

	@Override
	protected List<Token> getTokens(IDBService dbService) {
		return dbService.getTokens(true);
	}

	private Single<TransactionReceipt> transferTokens(Credentials credentials, String target, String quantity) {
		if (getTokenModel().getObject().isEthereumCurrency()) {
			return transferCurrency(credentials, target, new BigDecimal(quantity));
		}
		return transferTokens(credentials, target, new BigInteger(quantity));
	}

	private Single<TransactionReceipt> transferTokens(Credentials credentials, String target, BigInteger amount) {
		return service.loadSmartContract(credentials, getTokenModel().getObject())
				.transfer(target, amount);
	}

	private Single<TransactionReceipt> transferCurrency(Credentials credentials, String target, BigDecimal amount) {
        String unitName = getTokenModel().getObject().getName("en");
        Convert.Unit unit = Convert.Unit.fromString(unitName);
        return service.transferCurrency(credentials, target, amount, unit);
	}

	private Credentials readWallet(String password) {
		try {
			return service.readWallet(password, getWalletModel().getObject().getWalletJSON()).toBlocking().value();
		} catch (Exception ex) {
			LOG.error("Password is wrong!", ex);
		}
		return null;
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
