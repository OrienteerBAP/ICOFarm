package org.orienteer.component;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.component.BootstrapSize;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AjaxCommand;

import java.util.Optional;

public class InfoMessagePanel extends GenericPanel<String> {

    public InfoMessagePanel(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("description", getModel()));
        add(createOkButton("okButton"));
        setOutputMarkupPlaceholderTag(true);
    }

    private AjaxCommand<Void> createOkButton(String id) {
        return new AjaxCommand<Void>(id, new ResourceModel("info.button.ok")) {

            @Override
            protected void onInstantiation() {
                super.onInstantiation();
                setBootstrapType(BootstrapType.PRIMARY);
                setIcon(FAIconType.check);
                setBootstrapSize(BootstrapSize.SMALL);
            }

            @Override
            public void onClick(Optional<AjaxRequestTarget> targetOptional) {
                targetOptional.ifPresent(t -> onOkClick(t));
            }
        };
    }

    protected void onOkClick(AjaxRequestTarget target) {

    }
}
