package org.orienteer.component;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.util.ComponentUtils;
import org.orienteer.util.ICOFarmUtils;
import org.web3j.utils.Convert;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;
import rx.Observable;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

public class WalletPanel extends GenericPanel<Wallet> {

    public static final CssResourceReference WALLET_PANEL_CSS = new CssResourceReference(WalletPanel.class, "wallet-panel.css");

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    public WalletPanel(String id, IModel<Wallet> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Wallet wallet = getModelObject();
        String name = wallet.getName();
        add(new Label("title", !Strings.isNullOrEmpty(name) ? name : wallet.getAddress()));
        add(createBalanceLabel("balance"));
        add(createDeleteCommand("deleteCommand"));
        add(createDetailsLink("detailsLink"));
        add(createRefillLink("refillLink"));
        add(createSelectToken("selectToken"));
        setOutputMarkupPlaceholderTag(true);
    }

    protected void onWalletDelete(AjaxRequestTarget target) {

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
                Wallet wallet = WalletPanel.this.getModelObject();
                Token token = wallet.getDisplayableToken();
                String balance = getDisplayableBalance(wallet, token);
                setDefaultModelObject(balance);
            }

            private String getDisplayableBalance(Wallet wallet, Token token) {
                Observable<BigInteger> balanceObs = ethService.requestBalance(wallet.getAddress(), token);
                String balance = balanceObs.toBlocking().first().toString();
                if (ICOFarmUtils.isEthereumCurrency(token)) {
                    Convert.Unit unit = Convert.Unit.fromString(token.getName(Locale.ENGLISH.toLanguageTag()));
                    balance = Convert.fromWei(balance, unit).toString();
                }
                return balance;
            }
        };
    }

    private AjaxLink<ODocument> createDeleteCommand(String id) {
        return new AjaxLink<ODocument>(id, getDocumentModel()) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                ODatabaseDocument db = OrienteerWebSession.get().getDatabase();
                db.delete(getModelObject());
                db.commit(true);
                onWalletDelete(target);
            }
        };
    }

    private Command<ODocument> createRefillLink(String id) {
        return new AbstractModalWindowCommand<ODocument>(id, new ResourceModel("refill.link"), getDocumentModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setRenderBodyOnly(true);
                getLink().add(AttributeModifier.replace("class", "card-link float-right"));
            }

            @Override
            protected void initializeContent(ModalWindow modal) {
                modal.setMinimalWidth(580);
                modal.setMinimalHeight(370);
                modal.setContent(new RefillWalletPopupPanel(modal.getContentId(), getModel()));
            }
        };
    }

    private Link<ODocument> createDetailsLink(String id) {
        return new Link<ODocument>(id, getDocumentModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(new ResourceModel("details.link"));
            }

            @Override
            public void onClick() {
                setResponsePage(new ODocumentPage(getModel()));
            }
        };
    }

    private Component createSelectToken(String id) {
        List<Token> tokens = dbService.getTokens();
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
                        Wallet wallet = WalletPanel.this.getModelObject();
                        Token token = getModelObject();
                        wallet.setDisplayableToken(token.getDocument());
                        wallet.save();

                        target.add(WalletPanel.this);
                    }
                };
            }
        };
    }

    private IModel<ODocument> getDocumentModel() {
        ODocument doc = getModel().getObject().getDocument();
        return new ODocumentModel(doc);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(WALLET_PANEL_CSS));
    }
}
