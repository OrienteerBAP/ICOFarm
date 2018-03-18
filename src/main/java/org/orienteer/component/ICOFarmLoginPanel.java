package org.orienteer.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

public class ICOFarmLoginPanel extends AbstractICOFarmLoginPanel {
    public ICOFarmLoginPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        SignInPanel signInPanel = new SignInPanel("signInPanel", true);
        Form form = getForm(signInPanel);
        configFeedbackPanel((FeedbackPanel) signInPanel.get("feedback"));
        configInputField(form, "username", new ResourceModel("login.email.placeholder").getObject());
        configInputField(form, "password", new ResourceModel("login.password.placeholder").getObject());
        form.add(AttributeModifier.replace("class", "center-block form-horizontal"));
        form.setOutputMarkupId(true);
        signInPanel.setRememberMe(false);
        add(signInPanel);
        setOutputMarkupPlaceholderTag(true);
    }

    @Override
    protected void configFeedbackPanel(FeedbackPanel panel) {
        super.configFeedbackPanel(panel);
        if (isActivateAccount()) {
            success(new ResourceModel("login.registration").getObject());
        } else if (isRestorePasswordSuccess()) {
            success(new ResourceModel("login.restore").getObject());
        }
    }

    private boolean isActivateAccount() {
        return getWebPage().getPageParameters().get("registration").toString("").equals("success");
    }

    private boolean isRestorePasswordSuccess() {
        return getWebPage().getPageParameters().get("restore").toString("").equals("success");
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
