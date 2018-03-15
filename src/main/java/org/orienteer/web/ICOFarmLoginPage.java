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
        add(currentPanel);
        add(new Label("loginTitle", new ResourceModel("login.title")));
        add(new AjaxLink<String>("actionLink", new ResourceModel("login.action.restorePassword")) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                final boolean isLogin = currentPanel instanceof ICOFarmLoginPanel;
                Component title = get("loginTitle");
                title.setDefaultModelObject(getTitleKey(isLogin));
                currentPanel = currentPanel.replaceWith(getNextPanel(isLogin));
                setModelObject(getLabelKey(isLogin));
                target.add(currentPanel);
                target.add(title);
                target.add(this);
            }

            private Component getNextPanel(boolean isLogin) {
                return isLogin ? new ICOFarmRestorePasswordPanel(currentPanel.getId())
                        : new ICOFarmLoginPanel(currentPanel.getId());
            }

            private String getLabelKey(boolean isLogin) {
                return isLogin ? "login.action.restorePassword" : "login.action.login";
            }

            private String getTitleKey(boolean isLogin) {
                return isLogin ? "restore.title" : "login.title";
            }
        });
    }

    @Override
    public IModel<String> getTitleModel() {
        return new ResourceModel("login.page.title");
    }
}
