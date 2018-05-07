package org.orienteer.component.transaction;

import com.google.common.base.Strings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.model.Token;

public class LoadTokenTransactionsPanel extends GenericPanel<Token> {

    private boolean loading;

    public LoadTokenTransactionsPanel(String id, IModel<Token> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form<?> form = createForm("form");
        form.add(createHashField("hash"));
        form.add(createSubmitButton("submit"));
        add(form);
        add(createLabel("loadTitle"));
        add(createLoadingInfoContainer("loadingInfo"));
        setOutputMarkupPlaceholderTag(true);
    }

    private Form<?> createForm(String id) {
        return new Form<Object>(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!loading);
            }
        };
    }

    private Label createLabel(String id) {
        return new Label(id, getTitleModel()) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!loading);
            }
        };
    }

    private TextField<String> createHashField(String id) {
        return new RequiredTextField<>(id);
    }

    private AjaxButton createSubmitButton(String id) {
        return new AjaxButton(id, new ResourceModel("load.token.transactions.submit")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                loading = true;
                target.add(LoadTokenTransactionsPanel.this);
            }
        };
    }

    private WebMarkupContainer createLoadingInfoContainer(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(loading);
            }
        };
    }

    private IModel<String> getTitleModel() {
        Token token = getModelObject();
        String name = token.getName(getLocale().getLanguage());
        return Strings.isNullOrEmpty(name) ? Model.of(token.getName("en")) : Model.of(name);
    }
}
