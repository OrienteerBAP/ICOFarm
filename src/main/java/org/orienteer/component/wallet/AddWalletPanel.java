package org.orienteer.component.wallet;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.command.modal.SelectSubOClassDialogPage;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.web.ODocumentPage;
import org.orienteer.model.Wallet;

import java.util.Collection;

public class AddWalletPanel extends GenericPanel<OClass> {

    private ModalWindow modal;

    public AddWalletPanel(String id, IModel<OClass> model) {
        super(id, model);

        if (!model.getObject().isSubClassOf(Wallet.CLASS_NAME))
            throw new IllegalStateException("Panel " + getClass() + " need subclass of " + Wallet.CLASS_NAME);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("title", new ResourceModel("widget.wallet.add")));
        add(createClickBehavior());
        add(modal = createModalWindow("modal"));
    }

    private AjaxEventBehavior createClickBehavior() {
        return new AjaxEventBehavior("click") {
            @Override
            protected void onEvent(AjaxRequestTarget target) {
                OClass oClass = getModel().getObject();
                Collection<OClass> subClasses = oClass.getSubclasses();

                if(subClasses == null || subClasses.isEmpty()) {
                    redirectToCreateODocumentPage(oClass);
                } else {
                    modal.show(target);
                }
            }
        };
    }

    private ModalWindow createModalWindow(String id) {
        ModalWindow modal = new ModalWindow(id);
        modal.setTitle(new ResourceModel("dialog.select.class"));
        modal.setAutoSize(true);
        modal.setMinimalWidth(300);
        modal.setContent(new SelectSubOClassDialogPage(modal, getModel()) {
            @Override
            protected void onSelect(AjaxRequestTarget target, OClass oClass) {
                redirectToCreateODocumentPage(oClass);
            }
        });

        return modal;
    }

    private void redirectToCreateODocumentPage(OClass oClass) {
        ODocument doc = new ODocument(oClass);
        setResponsePage(new ODocumentPage(doc).setModeObject(DisplayMode.EDIT));
    }
}
