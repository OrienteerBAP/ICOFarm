package org.orienteer;

import com.orientechnologies.orient.core.hook.ORecordHook;
import com.orientechnologies.orient.core.metadata.schema.OType;
import org.apache.wicket.markup.html.WebPage;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.service.IFilterPredicateFactory;
import org.orienteer.hook.OWidgetHook;
import org.orienteer.hook.TokenHook;
import org.orienteer.hook.WalletHook;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.module.ICOFarmSecurityModule;
import org.orienteer.resource.ICOFarmReferralResource;
import org.orienteer.resource.ICOFarmRegistrationResource;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.service.ICOFarmFilterPredicateFactory;
import org.orienteer.web.ICOFarmLoginPage;

import java.util.List;

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
		registerModule(ICOFarmSecurityModule.class);
		registerModule(ICOFarmPerspectiveModule.class);
		ICOFarmReferralResource.mount(this);
		ICOFarmRegistrationResource.mount(this);
		ICOFarmRestorePasswordResource.mount(this);

		List<Class<? extends ORecordHook>> hooks = getOrientDbSettings().getORecordHooks();
		hooks.add(OWidgetHook.class);
		hooks.add(WalletHook.class);
		hooks.add(TokenHook.class);
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
	protected Class<? extends WebPage> getSignInPageClass() {
		return ICOFarmLoginPage.class;
	}
}
