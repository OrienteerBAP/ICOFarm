package org.orienteer;

import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import org.apache.wicket.request.Request;
import org.orienteer.core.OrienteerWebSession;

public class ICOFarmWebSession extends OrienteerWebSession {

    public ICOFarmWebSession(Request request) {
        super(request);
    }

    @Override
    public OSecurityUser getUser() {
        OSecurityUser user = super.getUser();
        return user != null ? user : getDatabase().getUser();
    }
}
