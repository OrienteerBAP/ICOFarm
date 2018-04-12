package org.orienteer.resource;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.web.HomePage;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.service.IDBService;
import org.orienteer.web.ICOFarmLoginPage;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ICOFarmRegistrationResource extends AbstractResource {

    public static final String MOUNT_PATH = "/registration/${id}";
    public static final String RES_KEY = ICOFarmRegistrationResource.class.getName();

    @Inject
    private IDBService dbService;

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
                activateUser(id);
                OrienteerWebSession.get().signOut();
                redirectToLoginPage();
            }

            private void activateUser(String id) {
                ICOFarmUser user = getUserById(id);
                dbService.updateUserStatus(user, true);
                dbService.createEmbeddedWalletForUser(user);
            }

            private void redirectToLoginPage() {
                PageParameters params = createPageParameters();
                RequestCycle.get().setResponsePage(ICOFarmLoginPage.class, params);
            }

            private PageParameters createPageParameters() {
                PageParameters params = new PageParameters();
                params.add("registration", "success");
                return params;
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
        ICOFarmUser user = getUserById(id);
        return user != null && user.getAccountStatus() != OUser.STATUSES.ACTIVE;
    }

    private ICOFarmUser getUserById(String id) {
        return dbService.getUserBy(ICOFarmUser.ID, id);
    }

    public static void mount(OrienteerWebApplication app) {
        app.getSharedResources().add(RES_KEY, app.getServiceInstance(ICOFarmRegistrationResource.class));
        app.mountResource(MOUNT_PATH, new SharedResourceReference(RES_KEY));
    }
}
