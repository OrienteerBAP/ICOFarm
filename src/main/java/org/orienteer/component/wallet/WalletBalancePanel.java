package org.orienteer.component.wallet;

import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.util.ComponentUtils;

import java.util.List;

public class WalletBalancePanel extends GenericPanel<Wallet> {

    public static final CssResourceReference WALLET_BALANCE_PANEL_CSS = new CssResourceReference(WalletBalancePanel.class, "wallet-panel.css");

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    public WalletBalancePanel(String id, IModel<Wallet> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        WebMarkupContainer container = createTitleContainer("titleContainer");
        container.add(createBalanceLabel("balance"));
        add(container);
        add(createSelectToken("selectToken"));
        setOutputMarkupPlaceholderTag(true);
    }

    protected WebMarkupContainer createTitleContainer(String id) {
        return new WebMarkupContainer(id);
    }

    private Label createBalanceLabel(String id) {
        return new Label(id, Model.of()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupId(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                Wallet wallet = WalletBalancePanel.this.getModelObject();
                Token token = wallet.getDisplayableToken();
                String balance = getDisplayableBalance(wallet, token);
                setDefaultModelObject(balance);
            }

            private String getDisplayableBalance(Wallet wallet, Token token) {
                return ethService.requestBalance(wallet.getAddress(), token).toBlocking().value().toString();
            }
        };
    }

    private Component createSelectToken(String id) {
        List<Token> tokens = dbService.getTokens(true);
        Token token = getModelObject().getDisplayableToken();
        IModel<Token> tokenModel = Model.of(tokens.get(tokens.indexOf(token)));

        return new DropDownChoice<Token>(id, tokenModel, tokens, ComponentUtils.getChoiceRendererForTokens()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(createOnChangeBehavior());
            }

            private AjaxFormComponentUpdatingBehavior createOnChangeBehavior() {
                return new AjaxFormComponentUpdatingBehavior("change") {

                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        Wallet wallet = WalletBalancePanel.this.getModelObject();
                        Token token = getModelObject();
                        wallet.setDisplayableToken(token.getDocument());
                        wallet.save();

                        target.add(WalletBalancePanel.this);
                    }
                };
            }
        };
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(WALLET_BALANCE_PANEL_CSS));
    }
}
