package org.orienteer.component;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.model.Wallet;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;

public class WalletPanel extends GenericPanel<Wallet> {

    public WalletPanel(String id, IModel<Wallet> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Wallet wallet = getModelObject();
        add(new Label("title", wallet.getAddress()));
        add(new Label("balance", wallet.getBalance()));
        add(createDeleteCommand("deleteCommand"));
        add(createDetailsLink("detailsLink"));
        add(createRefillLink("refillLink"));
        setRenderBodyOnly(true);
        setOutputMarkupPlaceholderTag(true);
    }

    protected void onWalletDelete(AjaxRequestTarget target) {

    }

    private AjaxLink<ODocument> createDeleteCommand(String id) {
        return new AjaxLink<ODocument>(id, getDocumentModel()) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                ODatabaseDocument db = OrienteerWebSession.get().getDatabase();
                db.delete(getModelObject());
                db.commit(true);
                onWalletDelete(target);
            }
        };
    }

    private Command<ODocument> createRefillLink(String id) {
        return new AbstractModalWindowCommand<ODocument>(id, new ResourceModel("refill.link"), getDocumentModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setRenderBodyOnly(true);
                getLink().add(AttributeModifier.replace("class", "card-link float-right"));
            }

            @Override
            protected void initializeContent(ModalWindow modal) {
                modal.setMinimalWidth(580);
                modal.setMinimalHeight(370);
                modal.setContent(new RefillWalletPopupPanel(modal.getContentId(), getModel()));
            }
        };
    }

    private Link<ODocument> createDetailsLink(String id) {
        return new Link<ODocument>(id, getDocumentModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(new ResourceModel("details.link"));
            }

            @Override
            public void onClick() {
                setResponsePage(new ODocumentPage(getModel()));
            }
        };
    }

    private IModel<ODocument> getDocumentModel() {
        ODocument doc = getModel().getObject().getDocument();
        return new ODocumentModel(doc);
    }
}
