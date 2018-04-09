package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OPropertyValueColumn;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.resource.ICOFarmReferralResource;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.LinkedList;
import java.util.List;

import static org.orienteer.module.ICOFarmModule.OPROPERTY_REFERRAL_CREATED;
import static org.orienteer.module.ICOFarmModule.OPROPERTY_REFERRAL_USER;

@Widget(id = ICOFarmModule.REFERRAL_WIDGET_ID, domain = "browse", selector = ICOFarmModule.REFERRAL, autoEnable = true)
public class ICOFarmReferralsWidget extends AbstractICOFarmWidget<OClass> {

    public static final JavaScriptResourceReference COPY_JS = new JavaScriptResourceReference(ICOFarmReferralsWidget.class, "copy.js");

    public ICOFarmReferralsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        OSecurityUser user = OrienteerWebSession.get().getUser();
        String url = ICOFarmReferralResource.genReferralForUser(user);
        add(new Label("referralTitle", new ResourceModel("widget.referrals.title")));
        add(new TextField<>("referralLink", Model.of(url)).setOutputMarkupId(true));
        add(new WebMarkupContainer("copyContainer").setOutputMarkupId(true));
        add(new Label("totalBonus", getTotalBonus(user)));
        add(new Label("followers", getFollowers(user)));
        add(createTablePanel("followersTable"));
    }

    private GenericTablePanel<ODocument> createTablePanel(String id) {
        OClass oClass = getModelObject();
        OQueryDataProvider<ODocument> provider = createDataProvider(oClass);
        List<IColumn<ODocument, String>> columns = createColumns(oClass, DisplayMode.VIEW.asModel());
        GenericTablePanel<ODocument> tablePanel = new GenericTablePanel<>(id, columns, provider, 10);
        tablePanel.getDataTable().add(AttributeModifier.append("class", "table-bordered"));
        return tablePanel;
    }

    private List<IColumn<ODocument, String>> createColumns(OClass oClass, IModel<DisplayMode> mode) {
        List<IColumn<ODocument, String>> columns = new LinkedList<>();
        columns.add(new OPropertyValueColumn(oClass.getProperty(OPROPERTY_REFERRAL_CREATED), mode));
        columns.add(new OPropertyValueColumn(oClass.getProperty(OPROPERTY_REFERRAL_USER), mode) {
            @Override
            public void populateItem(Item<ICellPopulator<ODocument>> item, String componentId, IModel<ODocument> docModel) {
                ODocument user = DBClosure.sudo((db) -> docModel.getObject().field(OPROPERTY_REFERRAL_USER));
                String firstName = user.field(ICOFarmUser.FIRST_NAME);
                String lastName = user.field(ICOFarmUser.LAST_NAME);
                item.add(new Label(componentId, firstName + " " + lastName));
            }
        });
        return columns;
    }

    private OQueryDataProvider<ODocument> createDataProvider(OClass oClass) {
        OQueryDataProvider<ODocument> provider =  new OQueryDataProvider<>(
                String.format("select from %s where @this['by'] = :byUser", oClass.getName()));
        provider.setSort(OPROPERTY_REFERRAL_CREATED, SortOrder.ASCENDING);
        provider.setParameter("byUser", new ODocumentModel(OrienteerWebSession.get().getUserAsODocument()));
        return provider;
    }

    private IModel<String> getTotalBonus(OSecurityUser user) {
        String sql = String.format("select sum(count(*)) as number from %s where @this['by'] = ?", getModelObject().getName());
        List<ODocument> docs = query(new OSQLSynchQuery<>(sql), user.getDocument());
        return Model.of(getNumberAsString(docs, "number"));
    }

    private IModel<String> getFollowers(OSecurityUser user) {
        String sql = String.format("select count(*) as number from %s where @this['by'] = ?", getModelObject().getName());
        List<ODocument> docs = query(new OSQLSynchQuery<>(sql), user.getDocument());
        return Model.of(getNumberAsString(docs, "number"));
    }

    private String getNumberAsString(List<ODocument> docs, String name) {
        if (docs != null && !docs.isEmpty()) {
            Object obj = docs.get(0).field(name);
            return obj.toString();
        }
        return "0";
    }

    private List<ODocument> query(OSQLSynchQuery<ODocument> sql, Object... args) {
        return OrienteerWebSession.get().getDatabase().query(sql, args);
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.link);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.referrals.title");
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(COPY_JS));
        response.render(OnDomReadyHeaderItem.forScript(String.format("addAutoCopyOnElement('%s', '%s');",
                get("referralLink").getMarkupId(), get("copyContainer").getMarkupId())));
    }
}
