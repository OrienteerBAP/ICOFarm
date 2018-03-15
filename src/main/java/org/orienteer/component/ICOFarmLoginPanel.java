package org.orienteer.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ICOFarmLoginPanel extends Panel {
    public ICOFarmLoginPanel(String id) {
        super(id);
    }


    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("prompt", new ResourceModel("login.project.name")));
        SignInPanel signInPanel = new SignInPanel("signInPanel", true);
        Form form = getForm(signInPanel);
        configFeedbackPanel(signInPanel);
        configInputField(form, "username", "E-mail");
        configInputField(form, "password", "Password");
        form.add(AttributeModifier.replace("class", "center-block form-horizontal"));
        form.setOutputMarkupId(true);
        signInPanel.setRememberMe(false);
        add(signInPanel);
        setOutputMarkupPlaceholderTag(true);
    }

    private void configFeedbackPanel(SignInPanel signInPanel) {
        FeedbackPanel panel = (FeedbackPanel) signInPanel.get("feedback");
        panel.setMaxMessages(2);
        panel.setEscapeModelStrings(false);
        panel.add(AttributeModifier.append("class", "icofarm-login-feedback"));
        if (isActivateAccount()) {
            success(new ResourceModel("login.registration").getObject());
        }
    }

    private void configInputField(Form form, String id, String placeholder) {
        Component component = form.get(id);
        component.add(AttributeModifier.replace("class", "form-control"));
        component.add(AttributeModifier.replace("placeholder", placeholder));
    }


    private boolean isActivateAccount() {
        return getWebPage().getPageParameters().get("registration").toString("").equals("success");
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(getClass(), "login.js")));
        response.render(OnDomReadyHeaderItem.forScript(getFormConfigScript()));
    }

    private String getFormConfigScript() {
        return "configSignInForm('" + getForm().getMarkupId() + "');";
    }

    public Form getForm() {
        return getForm(get("signInPanel"));
    }

    private Form getForm(Component component) {
        return (Form) component.get("signInForm");
    }
}
