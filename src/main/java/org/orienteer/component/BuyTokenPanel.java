package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.orienteer.core.component.OrienteerFeedbackPanel;
import org.orienteer.core.web.OrienteerBasePage;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;

public class BuyTokenPanel extends Panel {

	private static final Logger LOG = LoggerFactory.getLogger(BuyTokenPanel.class);

	private final Wallet wallet;
	private final TokenCurrency currency;

	@Inject
	private IEthereumService service;

	public BuyTokenPanel(String id, Wallet wallet, TokenCurrency currency) {
		super(id);
		this.wallet = wallet;
		this.currency = currency;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		Form<?> form = new Form<>("form");
		PasswordTextField password = new PasswordTextField("password", Model.of());
		TextField<Integer> quantityField = new TextField<>("quantity", Model.of(), Integer.class);
		quantityField.add(RangeValidator.minimum(0));
		form.add(password);
		form.add(quantityField);
		form.add(createSubmitButton("submitButton"));
		add(form);
		add(createFeedbackPanel("feedback"));
		setOutputMarkupPlaceholderTag(true);
	}

	private AjaxButton createSubmitButton(String id) {
		return new AjaxButton(id, new ResourceModel("buyToken.submit")) {

			@Override
			@SuppressWarnings("unchecked")
			protected void onSubmit(AjaxRequestTarget target) {
				try {
				    Form<?> form = getForm();
					String password = ((TextField<String>) form.get("password")).getModelObject();
					int quantity = ((TextField<Integer>) form.get("quantity")).getModelObject();
					buyTokens(password, quantity);
                    onBuyTokens(target);
				} catch (Exception ex) {
					LOG.error("Can't buy token!", ex);
					error(ex.getMessage() + " ");
					target.add(BuyTokenPanel.this);
				}
			}

			@Override
			protected void onError(AjaxRequestTarget target) {
				target.add(BuyTokenPanel.this);
				super.onError(target);
			}
		};
	}

	private FeedbackPanel createFeedbackPanel(String id) {
		FeedbackPanel panel = new OrienteerFeedbackPanel(id) {
			@Override
			protected void onBeforeRender() {
				super.onBeforeRender();
				OrienteerBasePage parent = findParent(OrienteerBasePage.class);
				parent.getFeedbacks().getFeedbackMessagesModel().detach();
			}
		};
    	panel.setOutputMarkupPlaceholderTag(true);
        panel.setMaxMessages(2);
        panel.setEscapeModelStrings(false);
		return panel;
    }

    private void buyTokens(String password, int quantity) throws Exception {
		String tokenAddress = currency.getContractAddress();
		EthereumClientConfig config = service.getConfig();
		Credentials credentials = service.readWallet(password, wallet.getWalletJSON());
		BigInteger gasPrice = config.getGasPriceFor(currency);
		BigInteger gasLimit = config.getGasLimitFor(currency);
		service.buyTokens(credentials, tokenAddress, BigInteger.valueOf(quantity), gasPrice, gasLimit);
	}

	protected void onBuyTokens(AjaxRequestTarget target) {

	}

}
