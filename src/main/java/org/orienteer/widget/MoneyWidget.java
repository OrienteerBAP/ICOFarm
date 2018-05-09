package org.orienteer.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.token.TokenMoneyPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.service.IDBService;
import org.orienteer.util.ComponentUtils;

import java.util.List;

@Widget(
        id = ICOFarmPerspectiveModule.MONEY_WIDGET_ID,
        domain = "browse",
        selector = ICOFarmModule.MONEY,
        autoEnable = true
)
public class MoneyWidget extends AbstractWidget<OClass> {

    @Inject
    private IDBService dbService;

    public MoneyWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        TokenMoneyPanel panel = createTokenMoneyPanel("tokenMoneyPanel");

        DropDownChoice<Token> selectToken = createSelectToken("selectToken", panel);
        panel.setModel(selectToken.getModel());

        Form<?> form = new Form<>("form");
        form.add(selectToken);
        add(form);
        add(panel);
    }

    private DropDownChoice<Token> createSelectToken(String id, TokenMoneyPanel tokenMoneyPanel) {
        List<Token> tokens = dbService.getTokens(false);
        IModel<Token> token = !tokens.isEmpty() ? Model.of(tokens.get(0)) : Model.of();
        DropDownChoice<Token> select = new DropDownChoice<>(id, token, tokens,
                ComponentUtils.getChoiceRendererForTokens());
        select.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                tokenMoneyPanel.setModel(select.getModel());
                target.add(tokenMoneyPanel);
            }
        });
        return select;
    }

    private TokenMoneyPanel createTokenMoneyPanel(String id) {
        return new TokenMoneyPanel(id, Model.of()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(getModelObject() != null);
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.money);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("money.widget.title");
    }
}
