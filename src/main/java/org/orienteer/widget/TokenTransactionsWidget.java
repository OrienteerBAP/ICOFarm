package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.table.OPropertyValueColumn;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.core.component.widget.AbstractCalculatedDocumentsWidget;
import org.orienteer.core.component.widget.document.CalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Token;
import org.orienteer.module.ICOFarmPerspectiveModule;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.util.List;

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
    @SuppressWarnings("unchecked")
    protected void onInitialize() {
        super.onInitialize();
        GenericTablePanel<ODocument> tablePanel = (GenericTablePanel<ODocument>) get("tablePanel");
        OrienteerDataTable<ODocument, String> dataTable = tablePanel.getDataTable();
        adjustTableColumns((List<IColumn<ODocument, String>>) dataTable.getColumns());
        dataTable.getCommandsToolbar().setDefaultModel(getModel());
    }

    private void adjustTableColumns(List<IColumn<ODocument, String>> columns) {
        columns.removeIf(col -> {
            if (col instanceof OPropertyValueColumn) {
                IModel<OProperty> criteryModel = ((OPropertyValueColumn) col).getCriteryModel();
                return criteryModel.getObject().getName().equals(OTransaction.OPROPERTY_TO);
            }
            return false;
        });
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
