package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.ICOFarmModule;
import org.orienteer.component.TransactionPanel;
import org.orienteer.component.TransactionsPanel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AjaxCommand;
import org.orienteer.core.component.command.AjaxFormCommand;
import org.orienteer.core.component.command.Command;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.Transaction;
import ru.ydn.wicket.wicketorientdb.model.ODocumentModel;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Date;

@Widget(id = "transactions", domain = "browse", selector = ICOFarmModule.TRANSACTION)
public class ICOFarmTransactionsWidget extends AbstractICOFarmWidget<OClass> {

    private Component currentComponent;

    public ICOFarmTransactionsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(currentComponent = newTransactionsPanel("panel"));
    }

    private TransactionsPanel newTransactionsPanel(String id) {
        TransactionsPanel transactionsPanel = new TransactionsPanel(id, getModel(), getCurrentUser());
        transactionsPanel.getDataTable().addCommand(newCreateTransactionCommand(transactionsPanel));
        return transactionsPanel;
    }

    private TransactionPanel newTransactionPanel(String id, IModel<ODocument> documentIModel) {
        TransactionPanel transactionPanel = new TransactionPanel(id, documentIModel);
        transactionPanel.getStructureTable().addCommand(newCancelCommand(transactionPanel));
        transactionPanel.getStructureTable().addCommand(newSaveTransactionCommand(transactionPanel));
        return transactionPanel;
    }

    private Command<ODocument> newCreateTransactionCommand(TransactionsPanel panel) {
        return new AjaxCommand<ODocument>(new ResourceModel("widget.transactions.add"), panel.getDataTable()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                TransactionPanel transactionPanel = newTransactionPanel(currentComponent.getId(), newTransactionDocumentModel());
                currentComponent = currentComponent.replaceWith(transactionPanel);
                target.add(currentComponent);
            }

            private IModel<ODocument> newTransactionDocumentModel() {
                Transaction transaction = new Transaction();
                transaction.setDateTime(new Date());
                transaction.setOwner(getCurrentUser());
                return new ODocumentModel(transaction.getDocument());
            }

            @Override
            public String getIcon() {
                return FAIconType.plus.getCssClass();
            }

            @Override
            public BootstrapType getBootstrapType() {
                return BootstrapType.PRIMARY;
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(AttributeModifier.append("class", "text-uppercase"));
            }
        };
    }

    private Command<ODocument> newSaveTransactionCommand(TransactionPanel transactionPanel) {
        return new AjaxFormCommand<ODocument>(new ResourceModel("widget.transaction.add"), transactionPanel.getStructureTable()) {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                DBClosure.sudoSave(getModelObject());
                TransactionsPanel transactionsPanel = newTransactionsPanel(currentComponent.getId());
                currentComponent = currentComponent.replaceWith(transactionsPanel);
                target.add(currentComponent);
            }

            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(AttributeModifier.append("class", "text-uppercase"));
            }

            @Override
            public String getIcon() {
                return FAIconType.plus.getCssClass();
            }

            @Override
            public BootstrapType getBootstrapType() {
                return BootstrapType.PRIMARY;
            }
        };
    }

    private Command<ODocument> newCancelCommand(TransactionPanel transactionPanel) {
        return new AjaxCommand<ODocument>(new ResourceModel("widget.transaction.cancel"), transactionPanel.getStructureTable()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                TransactionsPanel transactionsPanel = newTransactionsPanel(currentComponent.getId());
                currentComponent = currentComponent.replaceWith(transactionsPanel);
                target.add(currentComponent);
            }

            @Override
            public String getIcon() {
                return FAIconType.times.getCssClass();
            }

            @Override
            public BootstrapType getBootstrapType() {
                return BootstrapType.DANGER;
            }
        };
    }

    private OSecurityUser getCurrentUser() {
        return OrienteerWebSession.get().getEffectiveUser();
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.dollar);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return Model.of("Transactions");
    }

}
