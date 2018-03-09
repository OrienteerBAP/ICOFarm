package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.ICOFarmModule;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.resource.ICOFarmReferralResource;

@Widget(id = "referrals-widget", domain = "browse", selector = ICOFarmModule.REFERRAL)
public class ICOFarmReferralsWidget extends AbstractICOFarmWidget<OClass> {
    public ICOFarmReferralsWidget(String id, IModel<OClass> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        String url = ICOFarmReferralResource.genReferralForUser(OrienteerWebSession.get().getEffectiveUser());
        add(new Label("referralTitle", new ResourceModel("widget.referrals.title")));
        add(new TextField<>("referralLink", Model.of(url)));
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.link);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return new ResourceModel("widget.referrals.title");
    }
}
