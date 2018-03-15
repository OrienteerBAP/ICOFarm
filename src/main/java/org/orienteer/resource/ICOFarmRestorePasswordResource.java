package org.orienteer.resource;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.ICOFarmUser;

public class ICOFarmRestorePasswordResource extends AbstractResource {

    public static final String MOUNT_PATH = "/restore/${id}/";
    public static final String RES_KEY    = ICOFarmRestorePasswordResource.class.getName();

    public static String getLinkForUser(ICOFarmUser user) {
        return getLinkForUser(user.getDocument());
    }

    public static String getLinkForUser(ODocument doc) {
        String id = doc.field("id");
        PageParameters params = new PageParameters();
        params.add("id", id);
        CharSequence url = RequestCycle.get().urlFor(new SharedResourceReference(RES_KEY), params);
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        return null;
    }

    public static void mount(OrienteerWebApplication app) {
        app.getSharedResources().add(RES_KEY, app.getServiceInstance(ICOFarmRestorePasswordResource.class));
        app.mountResource(MOUNT_PATH, new SharedResourceReference(RES_KEY));
    }
}
