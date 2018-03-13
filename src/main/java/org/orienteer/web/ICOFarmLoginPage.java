package org.orienteer.web;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authentication.panel.SignInPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.orienteer.core.MountPath;
import org.orienteer.core.web.LoginPage;

@MountPath("/login")
public class ICOFarmLoginPage extends LoginPage {

    public ICOFarmLoginPage() {
        super();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        get("prompt").setDefaultModel(getPromptModel());
        SignInPanel panel = (SignInPanel) get("signInPanel");
        Form form = (Form) panel.get("signInForm");
        configFeedbackPanel(panel);
        configInputField(form, "username", "E-mail");
        configInputField(form, "password", "Password");
        form.add(AttributeModifier.replace("class", "center-block form-horizontal"));
        form.setOutputMarkupId(true);
    }

    private void configFeedbackPanel(SignInPanel signInPanel) {
        FeedbackPanel panel = (FeedbackPanel) signInPanel.get("feedback");
        panel.setMaxMessages(2);
        panel.setEscapeModelStrings(false);
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
        return getPageParameters().get("registration").toString("").equals("success");
    }

    private IModel<String > getPromptModel() {
        return Model.of("<h1 class='text-center'><strong>" + new ResourceModel("login.project.name").getObject()
                + "</strong></h1>");
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(new CssResourceReference(getClass(), "login.css")));
        response.render(JavaScriptHeaderItem.forReference(new JavaScriptResourceReference(getClass(), "login.js")));
        response.render(OnDomReadyHeaderItem.forScript(getFormConfigScript()));
    }

    private String getFormConfigScript() {
        return "configSignInForm('" + getForm().getMarkupId() + "');";
    }

    private Form getForm() {
        return (Form) get("signInPanel").get("signInForm");
    }
}
