package org.orienteer.component.wallet;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.ValidationError;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.OrienteerFeedbackPanel;
import org.orienteer.core.web.OrienteerBasePage;
import org.orienteer.model.Wallet;

public class DeleteWalletPanel extends GenericPanel<Wallet> {
    public DeleteWalletPanel(String id, IModel<Wallet> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form<?> form = new Form<>("form");
        form.add(createPasswordField("password"));
        form.add(createConfirmButton("confirm"));
        add(new Label("description", new StringResourceModel("delete.wallet.description", getModel())));
        add(form);
        add(createFeedbackPanel("feedback"));
        setOutputMarkupId(true);
    }

    protected void onDelete(AjaxRequestTarget target) {

    }

    private FeedbackPanel createFeedbackPanel(String id) {
        return new OrienteerFeedbackPanel(id) {

            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                OrienteerBasePage parent = findParent(OrienteerBasePage.class);
                parent.getFeedbacks().getFeedbackMessagesModel().detach();
            }

            @Override
            public void onEvent(IEvent<?> event) {

            }
        };
    }

    private TextField<String> createPasswordField(String id) {
        return new PasswordTextField(id, Model.of()) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                add(validatable -> {
                    if (!OrienteerWebSession.get().getPassword().equals(validatable.getValue())) {
                        ValidationError error = new ValidationError();
                        error.setMessage(new ResourceModel("delete.wallet.wrong.password").getObject());
                        validatable.error(error);
                    }
                });
            }
        };
    }

    private AjaxSubmitLink createConfirmButton(String id) {
        return new AjaxSubmitLink(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(new ResourceModel("delete.wallet.button.confirm"));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                ODatabaseDocument db = OrienteerWebSession.get().getDatabase();
                db.delete(getModelObject().getDocument());
                db.commit(true);
                onDelete(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(DeleteWalletPanel.this);
                super.onError(target);
            }
        };
    }
}
