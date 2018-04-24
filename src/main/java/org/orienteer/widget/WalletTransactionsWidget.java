package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.widget.document.CalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmPerspectiveModule;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

@Widget(id = ICOFarmPerspectiveModule.WALLET_TRANSACTIONS_WIDGET_ID,
        domain = "document",
        selector = Wallet.CLASS_NAME,
        autoEnable = true,
        tab = ICOFarmPerspectiveModule.WALLET_TRANSACTIONS_TAB)
public class WalletTransactionsWidget extends CalculatedDocumentsWidget {

    public WalletTransactionsWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected String getSql() {
        return String.format("select from %s where @this['%s'] = $doc['%s'] or @this['%s'] = $doc['%s']",
                OTransaction.CLASS_NAME, OTransaction.OPROPERTY_FROM, Wallet.OPROPERTY_ADDRESS,
                OTransaction.OPROPERTY_TO, Wallet.OPROPERTY_ADDRESS);
    }

    @Override
    protected OClass getExpectedClass(OQueryDataProvider<ODocument> provider) {
        return getSchema().getClass(OTransaction.CLASS_NAME);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.transactions.title");
    }
}
