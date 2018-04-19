package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.OrienteerFeedbackPanel;
import org.orienteer.core.web.OrienteerBasePage;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BuyTokenPanel extends Panel {

	private static final Logger LOG = LoggerFactory.getLogger(BuyTokenPanel.class);

	private final IModel<Wallet> wallet;
	private final IModel<TokenCurrency> token;

	@Inject
	private IEthereumService service;

	@Inject
    private IDBService dbService;

	public BuyTokenPanel(String id, IModel<Wallet> wallet, IModel<TokenCurrency> token) {
		super(id);
		this.wallet = wallet;
		this.token = token;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		Form<?> form = new Form<>("form");
		PasswordTextField password = new PasswordTextField("password", Model.of());
		TextField<Integer> quantityField = new TextField<>("quantity", Model.of(), Integer.class);
		quantityField.add(RangeValidator.minimum(0));
		quantityField.setRequired(true);
		form.add(password);
		form.add(quantityField);
		form.add(createSubmitButton("submitButton"));
		form.add(createSelectWalletContainer("selectWalletContainer"));
		form.add(createSelectTokenContainer("selectTokenContainer"));
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
					error(new ResourceModel("buyToken.error").getObject());
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

    private WebMarkupContainer createSelectWalletContainer(String id) {
	    return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
				DropDownChoice<Wallet> select = new DropDownChoice<>("selectWallet", wallet, getUserWallets(), createChoiceRenderer());
				select.setRequired(true);
				add(select);
                add(new Label("selectWalletLabel", new ResourceModel("buyToken.select.wallet")));
                setOutputMarkupId(true);
                setVisible(wallet.getObject() == null);
            }

            private ChoiceRenderer<Wallet> createChoiceRenderer() {
                return new ChoiceRenderer<Wallet>() {
                    @Override
                    public Object getDisplayValue(Wallet wallet) {
                        return wallet.getName() + " - " + wallet.getBalance();
                    }
                };
            }

			private List<Wallet> getUserWallets() {
				return dbService.getUserWallets(OrienteerWebSession.get().getUserAsODocument());
			}
        };
    }

    private WebMarkupContainer createSelectTokenContainer(String id) {
		return new WebMarkupContainer(id) {
			@Override
			protected void onInitialize() {
				super.onInitialize();
				DropDownChoice<TokenCurrency> select = new DropDownChoice<>("selectToken", token, dbService.getTokenCurrency(), createChoiceRenderer());
				select.setRequired(true);
				add(select);
				add(new Label("selectTokenLabel", new ResourceModel("buyToken.select.token")));
				setOutputMarkupId(true);
				setVisible(token.getObject() == null);
			}

			private ChoiceRenderer<TokenCurrency> createChoiceRenderer() {
				return new ChoiceRenderer<TokenCurrency>() {
					@Override
					public Object getDisplayValue(TokenCurrency token) {
						Map<String, String> names = token.getNames();
						String name = names.get(OrienteerWebSession.get().getLocale().toLanguageTag());
						if (name == null) name = names.get(Locale.ENGLISH.toLanguageTag());
						return name + " - " + token.getSymbol();
					}
				};
			}
		};
	}

    private void buyTokens(String password, int quantity) throws Exception {
		String tokenAddress = token.getObject().getContractAddress();
		EthereumClientConfig config = service.getConfig();
		Credentials credentials = service.readWallet(password, wallet.getObject().getWalletJSON());
		BigInteger gasPrice = config.getGasPriceFor(token.getObject());
		BigInteger gasLimit = config.getGasLimitFor(token.getObject());
		service.buyTokens(credentials, tokenAddress, BigInteger.valueOf(quantity), gasPrice, gasLimit); // TODO: add state which displays status of buying tokens
	}

	protected void onBuyTokens(AjaxRequestTarget target) {

	}
}
