package org.orienteer.component.token;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.util.ComponentUtils;

import java.math.BigDecimal;
import java.util.List;

public class TokenMoneyPanel extends GenericPanel<Token> {

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    public TokenMoneyPanel(String id, IModel<Token> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form<?> form = new Form<>("form");
        Label moneyLabel = createMoneyLabel("money");
        DropDownChoice<Token> selectCurrency = createSelectCurrency("selectCurrency", moneyLabel);
        moneyLabel.setDefaultModel(selectCurrency.getModel());

        form.add(moneyLabel);
        form.add(selectCurrency);
        add(form);
        setOutputMarkupPlaceholderTag(true);
    }

    private DropDownChoice<Token> createSelectCurrency(String id, Label moneyLabel) {
        List<Token> currencyTokens = dbService.getCurrencyTokens();
        IModel<Token> ether = getEtherModelToken(currencyTokens);
        DropDownChoice<Token> select = new DropDownChoice<>(id, ether, currencyTokens, ComponentUtils.getChoiceRendererForTokens());
        select.setOutputMarkupId(true);

        select.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                moneyLabel.setDefaultModel(select.getModel());
                target.add(moneyLabel);
            }
        });
        return select;
    }

    private Label createMoneyLabel(String id) {
        return new Label(id) {
            @Override
            public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
                if (getModelObject() != null) {
                    Token currency = (Token) getDefaultModelObject();
                    BigDecimal value = ethService.requestBalance(getTokenAddress(), currency).toBlocking().value();
                    replaceComponentTagBody(markupStream, openTag, value.toString());
                }
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupId(true);
            }
        };
    }

    private IModel<Token> getEtherModelToken(List<Token> tokens) {
        for (Token token : tokens) {
            if (token.getSymbol().equals(ICOFarmModule.ETH)) {
                return Model.of(token);
            }
        }
        return Model.of();
    }

    private String getTokenAddress() {
        return getModelObject().getAddress();
    }
}
