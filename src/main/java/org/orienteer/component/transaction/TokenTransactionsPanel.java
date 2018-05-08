package org.orienteer.component.transaction;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OPropertyValueColumn;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Token;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.util.LinkedList;
import java.util.List;

public class TokenTransactionsPanel extends GenericPanel<Token> {

    private GenericTablePanel<ODocument> tablePanel;

    public TokenTransactionsPanel(String id, IModel<Token> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        OQueryDataProvider<ODocument> provider = createDataProvider();
        add(new Label("transactionsCount", provider.size()));
        add(tablePanel = createTablePanel("transactionsTable", provider));
        setOutputMarkupPlaceholderTag(true);
    }

    public GenericTablePanel<ODocument> getTablePanel() {
        return tablePanel;
    }

    private GenericTablePanel<ODocument> createTablePanel(String id, OQueryDataProvider<ODocument> provider) {
        List<IColumn<ODocument, String>> columns = createColumns();
        GenericTablePanel<ODocument> tablePanel = new GenericTablePanel<>(id, columns, provider, 20);
        OrienteerDataTable<ODocument, String> dataTable = tablePanel.getDataTable();

        return tablePanel;
    }

    private Command<Void> createLoadTransactionsCommand() {
        return null;
    }

    private OQueryDataProvider<ODocument> createDataProvider() {
        String sql = String.format("select from %s where %s = :token", OTransaction.CLASS_NAME, OTransaction.OPROPERTY_TO);
        OQueryDataProvider<ODocument> provider = new OQueryDataProvider<>(sql);
        provider.setParameter("token", Model.of(getModelObject().getAddress().toLowerCase()));
        return provider;
    }

    private List<IColumn<ODocument, String>> createColumns() {
        OClass oClass = OrienteerWebSession.get().getDatabase().getMetadata().getSchema().getClass(OTransaction.CLASS_NAME);
        IModel<DisplayMode> modeModel = DisplayMode.VIEW.asModel();
        List<IColumn<ODocument, String>> columns = new LinkedList<>();

        columns.add(new OPropertyValueColumn(oClass.getProperty(OTransaction.OPROPERTY_TIMESTAMP), modeModel));
        columns.add(new OPropertyValueColumn(oClass.getProperty(OTransaction.OPROPERTY_HASH), modeModel));
        columns.add(new OPropertyValueColumn(oClass.getProperty(OTransaction.OPROPERTY_FROM), modeModel));
        columns.add(new OPropertyValueColumn(oClass.getProperty(OTransaction.OPROPERTY_VALUE), modeModel));
        columns.add(new OPropertyValueColumn(oClass.getProperty(OTransaction.OPROPERTY_TOKENS), modeModel));

        return columns;
    }
}
