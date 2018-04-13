package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Wallet;

@Widget(id = "wallets-widget", domain = "browse", selector = Wallet.CLASS_NAME, autoEnable = true)
public class WalletsWidget extends AbstractICOFarmWidget<OClass> {
    public WalletsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.shopping_bag);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return Model.of("Your wallets");
    }
}
