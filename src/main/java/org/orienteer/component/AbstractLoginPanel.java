package org.orienteer.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;

public abstract class AbstractLoginPanel extends Panel {
    public AbstractLoginPanel(String id) {
        super(id);
    }

    protected void configFeedbackPanel(FeedbackPanel panel) {
        panel.setMaxMessages(2);
        panel.setEscapeModelStrings(false);
        panel.add(AttributeModifier.append("class", "icofarm-login-feedback"));
    }

    protected void configInputField(Form form, String id, String placeholder) {
        configInputField(form.get(id), placeholder);
    }

    protected void configInputField(Component component, String placeholder) {
        component.add(AttributeModifier.replace("class", "form-control"));
        component.add(AttributeModifier.replace("placeholder", placeholder));
    }

}
