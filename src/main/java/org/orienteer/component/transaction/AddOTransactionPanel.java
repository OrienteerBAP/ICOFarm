package org.orienteer.component.transaction;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.ModalWindowFeedbackPanel;
import org.orienteer.model.OTransaction;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

public class AddOTransactionPanel extends Panel {

    @Inject
    private IDBService dbService;

    @Inject
    private IEthereumService ethService;

    public AddOTransactionPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form<?> form = new Form<>("form");
        form.add(new RequiredTextField<>("transactionHash", Model.of()));
        form.add(createSubmitLink("submit"));
        add(form);
        add(new ModalWindowFeedbackPanel("feedback"));
        setOutputMarkupPlaceholderTag(true);
    }

    private AjaxButton createSubmitLink(String id) {
        return new AjaxButton(id) {
            @Override
            @SuppressWarnings("unchecked")
            protected void onSubmit(AjaxRequestTarget target) {
                Form<?> form = getForm();
                String hash = ((TextField<String>) form.get("transactionHash")).getModelObject();
                if (!isTransactionExists(hash)) {
                    try {
                        Transaction transaction = ethService.requestTransactionByHash(hash);
                        EthBlock ethBlock = ethService.requestBlock(transaction.getBlockNumber().toString());
                        OTransaction oTransaction = dbService.saveTransaction(transaction, ethBlock.getBlock());
                        onAddTransaction(oTransaction, target);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        error(new ResourceModel("transaction.add.error").getObject());
                    }
                } else error(new ResourceModel("transaction.add.already.exists").getObject());

                if (hasErrorMessage()) onError(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(AddOTransactionPanel.this);
            }

            private boolean isTransactionExists(String hash) {
                return dbService.getTransactionByHash(hash) != null;
            }

        };
    }


    protected void onAddTransaction(OTransaction transaction, AjaxRequestTarget target) {

    }
}
