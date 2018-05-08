package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.widget.AbstractCalculatedDocumentsWidget;
import org.orienteer.core.component.widget.document.CalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmPerspectiveModule;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

@Widget(
        id = ICOFarmPerspectiveModule.TOKEN_TRANSACTIONS_WIDGET_ID,
        tab = ICOFarmPerspectiveModule.TOKEN_TRANSACTIONS_TAB,
        domain = "document",
        selector = Token.CLASS_NAME,
        autoEnable = true,
        oClass = AbstractCalculatedDocumentsWidget.WIDGET_OCLASS_NAME,
        order = 10
)
public class TokenTransactionsWidget extends CalculatedDocumentsWidget {

    public TokenTransactionsWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected String getSql() {
        return String.format("select from %s where @this['%s'] = $doc['%s']",
                OTransaction.CLASS_NAME, OTransaction.OPROPERTY_TO, Token.OPROPERTY_ADDRESS);
    }

    @Override
    protected OClass getExpectedClass(OQueryDataProvider<ODocument> provider) {
        return getSchema().getClass(OTransaction.CLASS_NAME);
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
