package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.InfoMessagePanel;
import org.orienteer.component.token.TransferTokenPanel;
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
        ModalWindow modal = createModalWindow("modal");
        add(createTransferTokenPanel("transferToken", modal));
        add(modal);
    }

    private ModalWindow createModalWindow(String id) {
        ModalWindow modal = new ModalWindow(id);
        modal.setMinimalWidth(370);
        modal.setAutoSize(true);
        return modal;
    }

    private TransferTokenPanel createTransferTokenPanel(String id, ModalWindow modal) {
        return new TransferTokenPanel(id, Model.of(), Model.of()) {
            @Override
            protected void onTransferTokens(AjaxRequestTarget target) {
                modal.setContent(new InfoMessagePanel(modal.getContentId(), new ResourceModel("transfer.token.success.text")) {
                    @Override
                    protected void onOkClick(AjaxRequestTarget target) {
                        modal.close(target);
                    }
                });
                modal.setTitle(new ResourceModel("info.title"));
                modal.show(target);
            }

            @Override
            protected Panel createFeedbackPanel(String id) {
                return new EmptyPanel(id);
            }
        };
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
