package org.orienteer.resource;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.apache.wicket.util.time.Time;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.web.HomePage;

import java.io.IOException;
import java.util.List;

public class ICOFarmReferralResource extends AbstractResource {

    public static final String MOUNT_PATH = "/referral/${id}/";
    public static final String RES_KEY    = ICOFarmReferralResource.class.getName();

    public static String genReferralForUser(OSecurityUser user) {
        String id = user.getDocument().field("id");
        PageParameters params = new PageParameters();
        params.add("id", id);
        CharSequence url = RequestCycle.get().urlFor(new SharedResourceReference(RES_KEY), params);
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
    }


    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        response.setLastModified(Time.now());
        if (response.dataNeedsToBeWritten(attributes)) {
            PageParameters params = attributes.getParameters();
            String id = params.get("id").toString("");
            WriteCallback callback = createCallback(!Strings.isNullOrEmpty(id) && isExistsInDatabase(id));
            response.setWriteCallback(callback);
        }
        return response;
    }

    private WriteCallback createCallback(boolean success) {
        return new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                if (success) {
                    PageParameters params = attributes.getParameters();
                    OrienteerWebSession.get().setAttribute("referral", params.get("id").toString(""));
                }
                RequestCycle.get().setResponsePage(HomePage.class);
            }
        };
    }

    private boolean isExistsInDatabase(String id) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase().query(new OSQLSynchQuery<>("select id from "
                + OUser.CLASS_NAME + " where id = ?", 1), id);
        return docs != null && !docs.isEmpty();
    }

    public static void mount(OrienteerWebApplication app) {
        app.getSharedResources().add(RES_KEY, app.getServiceInstance(ICOFarmReferralResource.class));
        app.mountResource(MOUNT_PATH, new SharedResourceReference(RES_KEY));
    }
}
