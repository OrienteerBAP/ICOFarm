package org.orienteer.component.token;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.OrienteerFeedbackPanel;
import org.orienteer.core.web.OrienteerBasePage;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.util.ComponentUtils;

import java.util.List;

public abstract class AbstractTokenPanel extends Panel {

    private final IModel<Wallet> walletModel;
    private final IModel<Token> tokenModel;

    @Inject
    private IDBService dbService;

    public AbstractTokenPanel(String id, IModel<Wallet> walletModel, IModel<Token> tokenModel) {
        super(id);
        this.walletModel = walletModel;
        this.tokenModel = tokenModel;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("panelTitle", getTitleModel()));
        Form<?> form = new Form<>("form");
        PasswordTextField password = new PasswordTextField("password", Model.of());
        TextField<String> quantityField = new TextField<>("quantity", Model.of());
        quantityField.setOutputMarkupId(true);
        form.add(password);
        form.add(quantityField);
        form.add(createSubmitButton("submitButton"));
        form.add(createSelectWalletContainer("selectWalletContainer"));
        form.add(createSelectTokenContainer("selectTokenContainer"));
        form.add(new Label("passwordLabel", getPasswordLabelModel()));
        form.add(new Label("quantityLabel", getQuantityLabelModel()));
        onInitialize(form);
        add(form);
        add(createFeedbackPanel("feedback"));
        setOutputMarkupPlaceholderTag(true);
    }

    protected void onInitialize(Form<?> form) {

    }

    private AjaxButton createSubmitButton(String id) {
        return new AjaxButton(id, getSubmitBtnLabelModel()) {

            @Override
            @SuppressWarnings("unchecked")
            protected void onSubmit(AjaxRequestTarget target) {
                onFormSubmit(target, getForm());
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                target.add(AbstractTokenPanel.this);
                super.onError(target);
            }
        };
    }

    private WebMarkupContainer createSelectWalletContainer(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                DropDownChoice<Wallet> select = new DropDownChoice<>("selectWallet", walletModel, getUserWallets(),
                        ComponentUtils.getChoiceRendererForWallets());
                select.add(new AjaxFormComponentUpdatingBehavior("change") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {}
                });
                select.setRequired(true);
                add(select);
                add(new Label("selectWalletLabel", getSelectWalletLabelModel()));
                setOutputMarkupId(true);
                setVisible(isSelectWalletContainerVisible());
            }

            private List<Wallet> getUserWallets() {
                return dbService.getUserWallets(OrienteerWebSession.get().getUserAsODocument());
            }
        };
    }

    private WebMarkupContainer createSelectTokenContainer(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                DropDownChoice<Token> select = new DropDownChoice<>("selectToken", tokenModel, getTokens(dbService),
                        ComponentUtils.getChoiceRendererForTokens());
                select.add(new AjaxFormComponentUpdatingBehavior("change") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {}
                });
                select.setRequired(true);
                add(select);
                add(new Label("selectTokenLabel", getSelectTokenLabelModel()));
                setOutputMarkupId(true);
                setVisible(isSelectTokenContainerVisible());
            }
        };
    }


    protected Panel createFeedbackPanel(String id) {
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

    protected abstract void onFormSubmit(AjaxRequestTarget target, Form<?> form);

    protected abstract List<Token> getTokens(IDBService dbService);

    protected abstract IModel<String> getTitleModel();
    protected abstract IModel<String> getPasswordLabelModel();
    protected abstract IModel<String> getSelectWalletLabelModel();
    protected abstract IModel<String> getSelectTokenLabelModel();
    protected abstract IModel<String> getQuantityLabelModel();
    protected abstract IModel<String> getSubmitBtnLabelModel();

    protected boolean isSelectWalletContainerVisible() {
        return walletModel.getObject() == null;
    }

    protected boolean isSelectTokenContainerVisible() {
        return tokenModel.getObject() == null;
    }

    protected IModel<Wallet> getWalletModel() {
        return walletModel;
    }

    protected IModel<Token> getTokenModel() {
        return tokenModel;
    }

}
