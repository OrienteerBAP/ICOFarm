package org.orienteer.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Token;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;

import static org.orienteer.module.ICOFarmPerspectiveModule.USER_STATISTICS_WIDGET_ID;

@Widget(
        id = USER_STATISTICS_WIDGET_ID,
        domain = "browse",
        selector = OUser.CLASS_NAME,
        autoEnable = true
)
public class UserStatisticsWidget extends AbstractWidget<OClass> {

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    private Token token;

    public UserStatisticsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        token = ethService.getConfig().getMainToken();
        add(createUsersCountLabel("usersCount"));
        add(createInvestorsCountLabel("investorsCount"));
    }

    private Label createUsersCountLabel(String id) {
        return new Label(id, Model.of()) {
            @Override
            protected void onConfigure() {
                setDefaultModelObject(dbService.getUsersCount());
                super.onConfigure();
            }
        };
    }

    private Label createInvestorsCountLabel(String id) {
        return new Label(id, Model.of()) {
            @Override
            protected void onConfigure() {
                setDefaultModelObject(dbService.getInvestorsCountFor(token));
                super.onConfigure();
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.users);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("user.statistics.widget.title");
    }
}
