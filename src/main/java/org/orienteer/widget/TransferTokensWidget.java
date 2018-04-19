package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.TransferTokenPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(id = ICOFarmPerspectiveModule.TRANSFER_TOKENS_WIDGET_ID, domain = "browse", selector = ICOFarmModule.TRANSFER_TOKENS, autoEnable = true)
public class TransferTokensWidget extends AbstractWidget<OClass> {

    public TransferTokensWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new TransferTokenPanel("transferToken", Model.of(), Model.of()));
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.exchange);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.transfer.tokens.title");
    }
}
