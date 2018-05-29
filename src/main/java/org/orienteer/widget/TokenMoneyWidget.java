package org.orienteer.widget;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.token.TokenMoneyPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(
        id = ICOFarmPerspectiveModule.TOKEN_MONEY_WIDGET_ID,
        domain = "document",
        selector = Token.CLASS_NAME,
        tab = ICOFarmPerspectiveModule.TOKEN_MONEY_TAB,
        autoEnable = true
)
public class TokenMoneyWidget extends AbstractWidget<ODocument> {
    public TokenMoneyWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Token token = new Token(getModelObject());
        add(new TokenMoneyPanel("tokenMoneyPanel", Model.of(token)));
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
