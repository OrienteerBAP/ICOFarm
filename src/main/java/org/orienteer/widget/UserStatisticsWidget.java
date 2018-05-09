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
import org.orienteer.service.IDBService;

@Widget(
        id = OUser.CLASS_NAME,
        domain = "browse",
        selector = OUser.CLASS_NAME,
        autoEnable = true
)
public class UserStatisticsWidget extends AbstractWidget<OClass> {

    @Inject
    private IDBService dbService;

    public UserStatisticsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(createUsersCountLabel("usersCount"));
        add(createInvestorsCountLabel("investorsCount"));
    }

    private Label createUsersCountLabel(String id) {
        return new Label(id, Model.of(16)) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
    }

    private Label createInvestorsCountLabel(String id) {
        return new Label(id, Model.of(9)) {
            @Override
            protected void onConfigure() {
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
