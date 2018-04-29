package org.orienteer.widget;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.wallet.WalletTableBalancePanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(id = ICOFarmPerspectiveModule.WALLET_BALANCE_WIDGET_ID,
        domain = "document",
        selector = Wallet.CLASS_NAME,
        autoEnable = true,
        tab = ICOFarmPerspectiveModule.WALLET_BALANCE_TAB)
public class WalletBalanceWidget extends AbstractWidget<ODocument> {

    public WalletBalanceWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ODocument doc = getModelObject();
        add(new WalletTableBalancePanel("balancePanel", Model.of(new Wallet(doc))));
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.money);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.tab.balance.title");
    }
}
