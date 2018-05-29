package org.orienteer.widget;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Token;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.service.web3.IICOFarmSmartContract;

import java.math.BigInteger;

import static org.orienteer.module.ICOFarmPerspectiveModule.TOKEN_TRANSACTIONS_INFO_WIDGET_ID;
import static org.orienteer.module.ICOFarmPerspectiveModule.TOKEN_TRANSACTIONS_TAB;

@Widget(
        id = TOKEN_TRANSACTIONS_INFO_WIDGET_ID,
        tab = TOKEN_TRANSACTIONS_TAB,
        domain = "document",
        selector = Token.CLASS_NAME,
        autoEnable = true,
        order = 0
)
public class TokenTransactionsInfoWidget extends AbstractWidget<ODocument> {

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    public TokenTransactionsInfoWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Token token = new Token(getModelObject());
        IModel<Token> model = Model.of(token);
        add(new Label("tokenName", getTokenName(model)));
        if (!token.isEthereumCurrency()) {
            add(createTransactionsCountLabel("transactionsCount", model));
            add(createAllTokensCountLabel("allTokensCount", model));
            add(createTokensSoldLabel("tokensSoldCount", model));
            add(createTokensRemainsLabel("tokensRemainsCount", model));
        } else {
            add(new Label("transactionsCount"));
            add(new Label("allTokensCount"));
            add(new Label("tokensSoldCount"));
            add(new Label("tokensRemainsCount"));
            setVisible(false);
        }
    }

    private Label createTransactionsCountLabel(String id, IModel<Token> token) {
        return new Label(id) {
            @Override
            protected void onConfigure() {
                setDefaultModelObject(dbService.getTokenTransactionsCount(token.getObject()));
                super.onConfigure();
            }
        };
    }

    private Label createAllTokensCountLabel(String id, IModel<Token> model) {
        return new Label(id) {
            @Override
            protected void onConfigure() {
                setDefaultModelObject(loadSmartContract(model).getTotalSupply().toBlocking().value());
                super.onConfigure();
            }
        };
    }

    private Label createTokensSoldLabel(String id, IModel<Token> token) {
        return new Label(id) {
            @Override
            protected void onConfigure() {
                setDefaultModelObject(dbService.getSoldTokensCount(token.getObject()));
                super.onConfigure();
            }
        };
    }

    private Label createTokensRemainsLabel(String id, IModel<Token> model) {
        return new Label(id) {
            @Override
            protected void onConfigure() {
                BigInteger totalSupply = loadSmartContract(model).getTotalSupply().toBlocking().value();
                BigInteger soldTokensCount = dbService.getSoldTokensCount(model.getObject());
                setDefaultModelObject(totalSupply.subtract(soldTokensCount));
                super.onConfigure();
            }
        };
    }

    private IICOFarmSmartContract loadSmartContract(IModel<Token> model) {
        Token token = model.getObject();
        return ethService.loadSmartContract(token.getOwner(), token);
    }

    private IModel<String> getTokenName(IModel<Token> model) {
        Token token = model.getObject();
        String name = token.getName(getLocale().getLanguage());
        return Strings.isNullOrEmpty(name) ? Model.of(token.getName("en")) : Model.of(name);
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.money);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("load.token.transactions.widget.title");
    }
}
