package org.orienteer.resource;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.web.HomePage;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ICOFarmRegistrationResource extends AbstractResource {

    public static final String MOUNT_PATH = "/registration/${id}";
    public static final String RES_KEY = ICOFarmRegistrationResource.class.getName();

    public static String genRegistrationLink(OSecurityUser user) {
        return genRegistrationLink(user.getDocument());
    }

    public static String genRegistrationLink(ODocument doc) {
        String id = doc.field("id");
        PageParameters params = new PageParameters();
        params.add("id", id);
        CharSequence url = RequestCycle.get().urlFor(new SharedResourceReference(RES_KEY), params);
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
    }

    @Override
    protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        if (response.dataNeedsToBeWritten(attributes)) {
            PageParameters params = attributes.getParameters();
            String id = params.get("id").toString(null);
            if (!Strings.isNullOrEmpty(id)) {
                if (isAccountNotActive(id)) {
                    response.setStatusCode(HttpServletResponse.SC_OK);
                    response.setWriteCallback(createSuccessCallback());
                }
            }
            if (response.getWriteCallback() == null) {
                response.setWriteCallback(createFailedCallback());
            }
        }
        return response;
    }

    private WriteCallback createSuccessCallback() {
        return new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                String id = attributes.getParameters().get("id").toString("");
                OSecurityUser user = activateUserAndGet(id);
                OrienteerWebSession.get().invalidate();
                OrienteerWebSession.get().authenticate(user.getName(), user.getPassword());
                RequestCycle.get().setResponsePage(HomePage.class);
            }

            private OSecurityUser activateUserAndGet(String id) {
                return new DBClosure<OSecurityUser>() {
                    @Override
                    protected OSecurityUser execute(ODatabaseDocument db) {
                        ODocument doc = getUserById(id);
                        doc.field("status", OUser.STATUSES.ACTIVE);
                        doc.save();
                        return new OUser(doc);
                    }
                }.execute();
            }
        };
    }

    private WriteCallback createFailedCallback() {
        return new WriteCallback() {
            @Override
            public void writeData(Attributes attributes) throws IOException {
                RequestCycle.get().setResponsePage(HomePage.class);
            }
        };
    }

    private boolean isAccountNotActive(String id) {
        ODocument user = getUserById(id);
        return user != null && !user.field("status").equals(OUser.STATUSES.ACTIVE);
    }

    private ODocument getUserById(String id) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where id = ?", 1), id);
        return docs != null && !docs.isEmpty() ? docs.get(0) : null;
    }

    public static void mount(OrienteerWebApplication app) {
        app.getSharedResources().add(RES_KEY, app.getServiceInstance(ICOFarmRegistrationResource.class));
        app.mountResource(MOUNT_PATH, new SharedResourceReference(RES_KEY));
    }
}
