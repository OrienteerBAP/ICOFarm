package org.orienteer.widget;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.orienteer.core.widget.AbstractWidget;

public abstract class AbstractICOFarmWidget<T> extends AbstractWidget<T> {

    public static final CssResourceReference ICOFARM_WIDGET_CSS = new CssResourceReference(AbstractICOFarmWidget.class, "icofarm-widget.css");

    public AbstractICOFarmWidget(String id, IModel<T> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(ICOFARM_WIDGET_CSS));
    }
}
