package org.orienteer;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.resource.ICOFarmReferralResource;
import org.orienteer.resource.ICOFarmRegistrationResource;

public class ICOFarmApplication extends OrienteerWebApplication
{
	@Override
	public void init()
	{
		super.init();
		mountPages("org.orienteer.web");
		registerWidgets("org.orienteer.widget");
		registerModule(ICOFarmModule.class);
		ICOFarmReferralResource.mount(this);
		ICOFarmRegistrationResource.mount(this);
	}

	@Override
	protected Class<? extends OrienteerWebSession> getWebSessionClass() {
		return ICOFarmWebSession.class;
	}
}
