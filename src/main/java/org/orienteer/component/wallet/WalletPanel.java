package org.orienteer.component.wallet;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.BootstrapSize;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
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
        String name = wallet.getName();
        add(new Label("title", !Strings.isNullOrEmpty(name) ? name : wallet.getAddress()));
        add(createWalletBalancePanel("balancePanel"));
        add(createDeleteCommand("deleteCommand"));
        add(createDetailsLink("detailsLink"));
        add(createRefillLink("refillLink"));
        setOutputMarkupPlaceholderTag(true);
    }

    protected void onWalletDelete(AjaxRequestTarget target) {

    }

    private WalletBalancePanel createWalletBalancePanel(String id) {
        return new WalletBalancePanel(id, getModel()) {
            @Override
            protected WebMarkupContainer createTitleContainer(String id) {
                WebMarkupContainer container = super.createTitleContainer(id);
                container.add(AttributeAppender.append("class", "card-text"));
                return container;
            }
        };
    }

    private AbstractModalWindowCommand<Wallet> createDeleteCommand(String id) {
        return new AbstractModalWindowCommand<Wallet>(id, new ResourceModel("widget.wallet.delete"), getModel()) {

            @Override
            protected void onInstantiation() {
                super.onInstantiation();
                setBootstrapType(BootstrapType.DANGER);
                setBootstrapSize(BootstrapSize.EXTRA_SMALL);
                setIcon(FAIconType.times);
            }

            @Override
            protected void initializeContent(ModalWindow modal) {
                modal.setAutoSize(true);
                modal.setMinimalWidth(400);
                modal.setTitle(new ResourceModel("wallet.modal.title"));
                modal.setContent(new DeleteWalletPanel(modal.getContentId(), getModel()) {
                    @Override
                    protected void onDelete(AjaxRequestTarget target) {
                        onWalletDelete(target);
                        modal.close(target);
                    }
                });
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
