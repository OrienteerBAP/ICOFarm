package org.orienteer.component;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.orienteer.model.Wallet;

import java.util.List;

public class WalletsRowPanel extends GenericPanel<List<Wallet>> {

    private final IModel<OClass> classModel;
    private final int size;

    public WalletsRowPanel(String id, IModel<OClass> classModel, IModel<List<Wallet>> model, int size) {
        super(id, model);
        this.size = size;
        this.classModel = classModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(createWalletsList("wallets"));
        add(createAddWalletPanel("addWalletPanel"));
        add(AttributeModifier.append("class", "row"));
        setOutputMarkupPlaceholderTag(true);
    }

    protected void onWalletDelete(AjaxRequestTarget target) {

    }

    private ListView<Wallet> createWalletsList(String id) {
        return new ListView<Wallet>(id, getModel()) {
            @Override
            protected void populateItem(ListItem<Wallet> listItem) {
                listItem.add(new WalletPanel("wallet", listItem.getModel()) {
                    @Override
                    protected void onWalletDelete(AjaxRequestTarget target) {
                        WalletsRowPanel.this.onWalletDelete(target);
                    }
                });
            }
        };
    }

    private AddWalletPanel createAddWalletPanel(String id) {
        return new AddWalletPanel(id, classModel) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(size > WalletsRowPanel.this.getModel().getObject().size());
            }
        };
    }
}
