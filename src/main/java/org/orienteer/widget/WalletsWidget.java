package org.orienteer.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.orienteer.component.wallet.WalletsRowPanel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.service.IDBService;
import org.orienteer.util.ICOFarmUtils;

import java.util.LinkedList;
import java.util.List;

@Widget(id = ICOFarmPerspectiveModule.WALLETS_WIDGET_ID, domain = "browse", selector = Wallet.CLASS_NAME, autoEnable = true)
public class WalletsWidget extends AbstractICOFarmWidget<OClass> {

    @Inject
    private IDBService dbService;

    public WalletsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        int size = 3;
        add(createListView("walletsContainer", size));
    }

    private ListView<List<Wallet>> createListView(String id, int size) {
        return new ListView<List<Wallet>>(id, new ListModel<>()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                ICOFarmUser user = new ICOFarmUser(OrienteerWebSession.get().getUser().getDocument());
                List<Wallet> wallets = dbService.getUserWallets(user);
                setModelObject(prepareWallets(wallets, size));
            }

            @Override
            protected void populateItem(ListItem<List<Wallet>> listItem) {
                listItem.setRenderBodyOnly(true);
                listItem.add(new WalletsRowPanel("walletsRow", WalletsWidget.this.getModel(), listItem.getModel(), size) {
                    @Override
                    protected void onWalletDelete(AjaxRequestTarget target) {
                        target.add(WalletsWidget.this);
                    }
                });
            }

            private List<List<Wallet>> prepareWallets(List<Wallet> wallets, int size) {
                List<List<Wallet>> result = wallets.stream().collect(ICOFarmUtils.getCollectorForGroupList(size));
                if (result.isEmpty() || result.get(result.size() - 1).size() == size) {
                    result.add(new LinkedList<>());
                }
                return result;
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.briefcase);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.wallets.title");
    }
}
