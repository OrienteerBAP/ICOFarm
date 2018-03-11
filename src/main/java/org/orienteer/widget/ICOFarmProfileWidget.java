package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.orienteer.component.ProfilePanel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;

@Widget(id = "icofarm-profile", domain = "document", selector = OUser.CLASS_NAME, autoEnable = true)
public class ICOFarmProfileWidget extends AbstractICOFarmWidget<ODocument> {

    public ICOFarmProfileWidget(String id, IModel<ODocument> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new ProfilePanel("profilePanel", new ODocumentModel(OrienteerWebSession.get().getUser().getDocument())));
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.user);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return null;
    }
}
