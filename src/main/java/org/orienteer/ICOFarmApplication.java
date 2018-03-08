package org.orienteer;

import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;

public class ICOFarmApplication extends OrienteerWebApplication
{
	@Override
	public void init()
	{
		super.init();
		mountPages("org.orienteer.web");
		registerWidgets("org.orienteer.widget");
		registerModule(ICOFarmModule.class);
	}

	@Override
	protected Class<? extends OrienteerWebSession> getWebSessionClass() {
		return ICOFarmWebSession.class;
	}
}
