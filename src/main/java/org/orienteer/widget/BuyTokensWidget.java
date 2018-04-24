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
import org.orienteer.component.token.BuyTokenPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(id = ICOFarmPerspectiveModule.BUY_TOKENS_WIDGET_ID, domain = "browse", selector = ICOFarmModule.BUY_TOKENS, autoEnable = true)
public class BuyTokensWidget extends AbstractWidget<OClass> {

    public BuyTokensWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        ModalWindow modal = createModalWindow("modal");
        add(createBuyTokenPanel("buyTokens", modal));
        add(modal);
    }

    private BuyTokenPanel createBuyTokenPanel(String id, ModalWindow modal) {
        return new BuyTokenPanel(id, Model.of(), Model.of()) {
            @Override
            protected void onBuyTokens(AjaxRequestTarget target) {
                modal.setContent(new InfoMessagePanel(modal.getContentId(), new ResourceModel("buy.token.success.text")) {
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

    private ModalWindow createModalWindow(String id) {
        ModalWindow modal = new ModalWindow(id);
        modal.setMinimalWidth(370);
        modal.setAutoSize(true);
        return modal;
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.dollar);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.buy.tokens.title");
    }
}
