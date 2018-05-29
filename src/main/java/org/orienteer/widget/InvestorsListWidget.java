package org.orienteer.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.widget.AbstractCalculatedDocumentsWidget;
import org.orienteer.core.component.widget.browse.CalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.service.web3.IEthereumService;

@Widget(
        id = ICOFarmPerspectiveModule.USER_INVESTORS_WIDGET_ID,
        domain = "browse",
        selector = ICOFarmUser.CLASS_NAME,
        autoEnable = true,
        oClass = AbstractCalculatedDocumentsWidget.WIDGET_OCLASS_NAME,
        order = 10
)
public class InvestorsListWidget extends CalculatedDocumentsWidget {

    @Inject
    private IEthereumService ethService;

    public InvestorsListWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected String getSql() {
        String symbol = getMainTokenSymbol();
        return String.format("select from %s where %s contains (%s['%s'] > 0)",
                ICOFarmUser.CLASS_NAME, ICOFarmUser.OPROPERTY_WALLETS, Wallet.OPROPERTY_BALANCES, symbol);
    }

    private String getMainTokenSymbol() {
        return ethService.getConfig().getMainToken().getSymbol();
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("user.statistics.widget.investors");
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.money);
    }
}
