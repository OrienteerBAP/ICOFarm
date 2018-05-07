package org.orienteer.widget;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.transaction.LoadTokenTransactionsPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(
        id = ICOFarmPerspectiveModule.OWNER_TRANSCTIONS_WIDGET_ID,
        selector = Token.CLASS_NAME,
        domain = "document",
        tab = "Owner Transactions",
        autoEnable = true
)
public class ICOFarmOwnerTokenTransactionsWidget extends AbstractWidget<ODocument> {

    public ICOFarmOwnerTokenTransactionsWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        IModel<Token> model = Model.of(new Token(getModelObject()));
        add(new LoadTokenTransactionsPanel("loadTokenTransactions", model));
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.align_justify);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.tab.token.transactions.title");
    }
}
