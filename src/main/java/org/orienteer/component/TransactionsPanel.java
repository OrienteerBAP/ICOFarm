package org.orienteer.component;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OPropertyValueColumn;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.model.Transaction;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;
import ru.ydn.wicket.wicketorientdb.model.OPropertyModel;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionsPanel extends AbstractICOFarmPanel<OClass> {

    private final OrienteerDataTable<ODocument, String> dataTable;

    public TransactionsPanel(String id, IModel<OClass> model, OSecurityUser user) {
        super(id, model);
        setOutputMarkupPlaceholderTag(true);
        IModel<DisplayMode> displayModeModel = DisplayMode.VIEW.asModel();
        List<IColumn<ODocument, String>> columns = newColumns(model.getObject(), displayModeModel);
        OQueryDataProvider<ODocument> provider = newQueryDataProvider(model.getObject(), user);
        GenericTablePanel<ODocument> tablePanel = new GenericTablePanel<>("transactions", columns, provider, 10);
        dataTable = tablePanel.getDataTable();
        dataTable.add(AttributeModifier.append("class", "table-bordered"));
        add(tablePanel);
    }

    private List<IColumn<ODocument, String>> newColumns(OClass oClass, IModel<DisplayMode> model) {
        return Transaction.getUserFields().stream()
                .map(field -> new OPropertyModel(oClass.getProperty(field)))
                .map(pModel -> new OPropertyValueColumn(pModel, model))
                .collect(Collectors.toList());
    }

    private OQueryDataProvider<ODocument> newQueryDataProvider(OClass oClass, OSecurityUser user) {
        OQueryDataProvider<ODocument> provider = new OQueryDataProvider<>("select from " + oClass.getName() + " where owner = :user");
        provider.setParameter("user", new ODocumentModel(user.getDocument()));
        provider.setSort("dateTime", SortOrder.ASCENDING);
        return provider;
    }


    public OrienteerDataTable<ODocument, String> getDataTable() {
        return dataTable;
    }

    @Override
    protected IModel<String> getTitle() {
        return new ResourceModel("widget.transactions.title");
    }

    @Override
    protected String getCssClasses() {
        return super.getCssClasses() + " center-block";
    }
}
