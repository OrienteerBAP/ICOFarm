package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.RegistrationPanel;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;

@Widget(id = ICOFarmPerspectiveModule.REGISTRATION_WIDGET_ID, domain = "browse", selector = ICOFarmModule.REGISTRATION, autoEnable = true)
public class ICOFarmRegistrationWidget extends AbstractICOFarmWidget<OSecurityUser> {



    public ICOFarmRegistrationWidget(String id, IModel<OSecurityUser> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        WebMarkupContainer container = new WebMarkupContainer("container");
        container.add(createRegistrationPanel("registrationPanel"));
        container.add(createSuccessTitle("successTitle"));
        container.add(createSuccessLabel("successLabel"));
        container.setOutputMarkupPlaceholderTag(true);
        add(container);
    }

    private RegistrationPanel createRegistrationPanel(String id) {
        return new RegistrationPanel(id) {
            @Override
            protected void onFormSubmit(AjaxRequestTarget target) {
                setVisible(false);
                Component container = ICOFarmRegistrationWidget.this.get("container");
                container.get("successTitle").setVisible(true);
                container.get("successLabel").setVisible(true);
                target.add(container);
            }
        };
    }

    private Label createSuccessTitle(String id) {
        return new Label(id, new ResourceModel("widget.registration.success.title")) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
                setVisible(false);
            }
        };
    }

    private Label createSuccessLabel(String id) {
        return new Label(id, new ResourceModel("widget.registration.success.content")) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
                setVisible(false);
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.user);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.registration.title");
    }

}
