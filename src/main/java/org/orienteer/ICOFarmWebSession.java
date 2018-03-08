package org.orienteer;

import com.google.common.base.Strings;
import org.apache.wicket.request.Request;
import org.orienteer.core.OrienteerWebSession;

public class ICOFarmWebSession extends OrienteerWebSession {

    public ICOFarmWebSession(Request request) {
        super(request);
        String username = System.getProperty("anonymous.username");
        String password = System.getProperty("anonymous.password");
        if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(password)) {
            authenticate(username, password);
        }
    }

}
