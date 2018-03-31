package org.orienteer;

import com.orientechnologies.orient.core.metadata.schema.OType;
import org.apache.wicket.markup.html.WebPage;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.service.IFilterPredicateFactory;
import org.orienteer.hook.ICOFarmOWidgetHook;
import org.orienteer.resource.ICOFarmReferralResource;
import org.orienteer.resource.ICOFarmRegistrationResource;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.service.ICOFarmFilterPredicateFactory;
import org.orienteer.web.ICOFarmLoginPage;

public class ICOFarmApplication extends OrienteerWebApplication {

	public static final CustomAttribute REMOVE_CRON_RULE              = CustomAttribute.create("remove.cron", OType.STRING, "", false, false);
	public static final CustomAttribute REMOVE_SCHEDULE_START_TIMEOUT = CustomAttribute.create("remove.timeout", OType.STRING, "0", false, false);

	private ICOFarmFilterPredicateFactory predicateFactory;

	@Override
	public void init() {
		super.init();
		mountPages("org.orienteer.web");
		registerWidgets("org.orienteer.widget");
		registerModule(ICOFarmModule.class);
		ICOFarmReferralResource.mount(this);
		ICOFarmRegistrationResource.mount(this);
		ICOFarmRestorePasswordResource.mount(this);

		getOrientDbSettings().getORecordHooks().add(ICOFarmOWidgetHook.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getServiceInstance(Class<T> serviceType) {
		if (IFilterPredicateFactory.class.equals(serviceType)) {
			if (predicateFactory == null) predicateFactory = new ICOFarmFilterPredicateFactory();
			return (T) predicateFactory;
		}
		return super.getServiceInstance(serviceType);
	}

	@Override
	protected Class<? extends OrienteerWebSession> getWebSessionClass() {
		return ICOFarmWebSession.class;
	}

	@Override
	protected Class<? extends WebPage> getSignInPageClass() {
		return ICOFarmLoginPage.class;
	}
}
