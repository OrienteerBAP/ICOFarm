package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.widget.AbstractCalculatedDocumentsWidget;
import org.orienteer.core.component.widget.browse.CalculatedDocumentsWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(
        id = ICOFarmPerspectiveModule.USERS_WIDGET_ID,
        domain = "browse",
        selector = ICOFarmUser.CLASS_NAME,
        autoEnable = true,
        oClass = AbstractCalculatedDocumentsWidget.WIDGET_OCLASS_NAME,
        order = 30
)
public class UsersListWidget extends CalculatedDocumentsWidget {
    public UsersListWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected String getSql() {
        return "select from " + OUser.CLASS_NAME;
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.user);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("user.statistics.widget.users");
    }
}
