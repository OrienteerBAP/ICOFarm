package org.orienteer.web;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.orienteer.core.web.BasePage;

public abstract class ICOFarmBasePage<T> extends BasePage<T> {

    public static final CssResourceReference ICOFARM_CSS = new CssResourceReference(ICOFarmBasePage.class, "icofarm.css");

    public ICOFarmBasePage() {
        super();
    }

    public ICOFarmBasePage(IModel<T> model) {
        super(model);
    }

    public ICOFarmBasePage(PageParameters parameters) {
        super(parameters);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(ICOFARM_CSS));
    }
}
