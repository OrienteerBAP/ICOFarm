package org.orienteer.component;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

public abstract class AbstractICOFarmPanel<T> extends GenericPanel<T> {
    public AbstractICOFarmPanel(String id) {
        super(id);
    }

    public AbstractICOFarmPanel(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Label label = new Label("title", getTitle());
        label.setOutputMarkupPlaceholderTag(true);
        label.add(AttributeModifier.append("class", getTitleCssClasses()));
        add(label);
        add(AttributeModifier.replace("class", getCssClasses()));
    }

    protected String getCssClasses() {
        return "container row";
    }

    protected String getTitleCssClasses() {
        return "";
    }

    protected abstract IModel<String> getTitle();
}
