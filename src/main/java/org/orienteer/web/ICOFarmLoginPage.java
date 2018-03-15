package org.orienteer.web;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.orienteer.component.ICOFarmLoginPanel;
import org.orienteer.component.ICOFarmRestorePasswordPanel;
import org.orienteer.core.MountPath;

@MountPath("/login")
public class ICOFarmLoginPage extends ICOFarmBasePage<Object> {

    private Component currentPanel;

    public ICOFarmLoginPage() {
        super();
    }

    public ICOFarmLoginPage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        currentPanel = new ICOFarmLoginPanel("panel");
        Label loginTitle = new Label("loginTitle", new ResourceModel("login.title"));
        loginTitle.setOutputMarkupId(true);
        add(currentPanel);
        add(loginTitle);
        add(createActionLink("actionLink"));
        add(new Label("prompt", new ResourceModel("login.project.name")));
    }

    private AjaxLink<String> createActionLink(String id) {
        return new AjaxLink<String>(id, new ResourceModel("login.action.forgotPassword")) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(getModel());
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onClick(AjaxRequestTarget target) {
                final boolean isLogin = currentPanel instanceof ICOFarmLoginPanel;
                Component title = ICOFarmLoginPage.this.get("loginTitle");
                title.setDefaultModel(new ResourceModel(getTitleKey(isLogin)));
                currentPanel = currentPanel.replaceWith(getNextPanel(isLogin));
                setBody(new ResourceModel(getLabelKey(isLogin)));
                target.add(currentPanel);
                target.add(title);
                target.add(this);
            }

            private Component getNextPanel(boolean isLogin) {
                return isLogin ? new ICOFarmRestorePasswordPanel(currentPanel.getId())
                        : new ICOFarmLoginPanel(currentPanel.getId());
            }

            private String getLabelKey(boolean isLogin) {
                return isLogin ? "login.action.login" : "login.action.forgotPassword";
            }

            private String getTitleKey(boolean isLogin) {
                return isLogin ? "restore.title" : "login.title";
            }
        };
    }

    @Override
    public IModel<String> getTitleModel() {
        return new ResourceModel("login.page.title");
    }
}
