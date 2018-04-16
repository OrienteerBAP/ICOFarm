package org.orienteer.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OIdentity;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Wallet;

import java.util.*;

import static org.orienteer.core.module.OWidgetsModule.*;
import static org.orienteer.module.ICOFarmModule.REFERRAL;
import static org.orienteer.module.ICOFarmModule.REGISTRATION;
import static org.orienteer.module.ICOFarmSecurityModule.*;

public class ICOFarmPerspectiveModule extends AbstractOrienteerModule {

    public static final String INVESTOR_PERSPECTIVE  = "Investor";
    public static final String ANONYMOUS_PERSPECTIVE = "Anonymous";

    public static final String REFERRAL_WIDGET_ID       = "referrals-widget";
    public static final String REGISTRATION_WIDGET_ID   = "registration";
    public static final String SCHEMA_CLASSES_WIDGET_ID = "list-oclasses";
    public static final String LIST_DOCUMENTS_WIDGET_ID = "list-all";
    public static final String WALLETS_WIDGET_ID        = "wallets-widget";

    /**
     * Contains hidden properties for investors.
     * key - class name
     * value - list with hidden properties
     */
    public static final Map<String, List<String>> HIDDEN_PROPERTIES = new HashMap<>();

    /**
     * Contains hidden widgets for investors
     * key - class name
     * value - hidden widget
     */
    public static final Map<String, List<String>> HIDDEN_WIDGETS = new HashMap<>();

    protected ICOFarmPerspectiveModule() {
        super("icofarm-perspective", ICOFarmModule.VERSION);
    }

    @Override
    public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
        OSchemaHelper helper = OSchemaHelper.bind(db);
        createPerspectives(helper);
        updatePerspectivesPermissions(helper, db);
        adjustDashboard(helper);
        return null;
    }

    @Override
    public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
        onInstall(app, db);
    }

    @Override
    public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
        initHiddenProperties();
        initHiddenWidgets();
    }

    private void createPerspectives(OSchemaHelper helper) {
        ODocument perspective = getOrCreatePerspective(INVESTOR_PERSPECTIVE, helper);
        perspective.field("icon", FAIconType.usd.name());
        perspective.field("homeUrl", "/browse/" + OTransaction.CLASS_NAME);
        perspective.save();

        ODocument item1 = getOrCreatePerspectiveItem("Transactions", perspective, helper);
        item1.field("icon", FAIconType.usd.name());
        item1.field("perspective", perspective);
        item1.field("url", "/browse/" + OTransaction.CLASS_NAME);
        item1.save();

        ODocument item2 = getOrCreatePerspectiveItem("Referrals", perspective, helper);
        item2.field("icon", FAIconType.users.name());
        item2.field("perspective", perspective);
        item2.field("url", "/browse/" + REFERRAL);
        item2.save();

        ODocument item3 = getOrCreatePerspectiveItem("Wallets", perspective, helper);
        item3.field("icon", FAIconType.briefcase.name());
        item3.field("perspective", perspective);
        item3.field("url", "/browse/" + Wallet.CLASS_NAME);
        item3.save();

        perspective.field("menu", Arrays.asList(item1, item2, item3));
        perspective.save();

        perspective = getOrCreatePerspective(ANONYMOUS_PERSPECTIVE, helper);
        perspective.field("icon", FAIconType.user_secret.name());
        perspective.field("homeUrl", "/browse/Registration");
        perspective.save();

        item1 = getOrCreatePerspectiveItem("Registration", perspective, helper);
        item1.field("icon", FAIconType.info.name());
        item1.field("perspective", perspective);
        item1.field("url", "/browse/Registration");
        item1.save();

        perspective.field("menu", Collections.singletonList(item1));
        perspective.save();
    }


    private void updatePerspectivesPermissions(OSchemaHelper helper, ODatabaseDocument db) {
        OClass restricted = helper.oClass("ORestricted").getOClass();
        OClass perspectiveClass = helper.oClass(PerspectivesModule.OCLASS_PERSPECTIVE).getOClass();

        if (!perspectiveClass.isSubClassOf(restricted)) {
            perspectiveClass.addSuperClass(restricted);
        }
        updateUser(db, ANONYMOUS_PERSPECTIVE, ORESTRICTED_ALLOW_READ, "reader");
        updateRole(db, INVESTOR_PERSPECTIVE, ORESTRICTED_ALLOW_READ, INVESTOR_ROLE);
    }

    private void adjustDashboard(OSchemaHelper helper) {
        createWidgetIfNotExists(REGISTRATION_WIDGET_ID, REGISTRATION, helper);

        createWidgetIfNotExists(REFERRAL_WIDGET_ID, REFERRAL, helper);
        createWidgetIfNotExists(LIST_DOCUMENTS_WIDGET_ID, REFERRAL, helper);

        createWidgetIfNotExists(WALLETS_WIDGET_ID, Wallet.CLASS_NAME, helper);
        createWidgetIfNotExists(LIST_DOCUMENTS_WIDGET_ID, Wallet.CLASS_NAME, helper);
    }

    private void initHiddenProperties() {
        List<String> userProperties = new ArrayList<>(9);
        userProperties.add(ICOFarmUser.ID);
        userProperties.add(ICOFarmUser.RESTORE_ID);
        userProperties.add(ICOFarmUser.RESTORE_ID_CREATED);
        userProperties.add("name");
        userProperties.add("online");
        userProperties.add("status");
        userProperties.add("lastSessionId");
        userProperties.add("roles");

        List<String> restrictedProperties = new ArrayList<>(4);
        restrictedProperties.add(ORESTRICTED_ALLOW);
        restrictedProperties.add(ORESTRICTED_ALLOW_READ);
        restrictedProperties.add(ORESTRICTED_ALLOW_UPDATE);
        restrictedProperties.add(ORESTRICTED_ALLOW_DELETE);

        List<String> identityProperties = new ArrayList<>(2);
        identityProperties.add("perspective");
        identityProperties.add("perspectiveItem");

        List<String> walletProperties = new ArrayList<>(1);
        walletProperties.add(Wallet.OPROPERTY_OWNER);
        walletProperties.add(Wallet.OPROPERTY_CURRENCY);

        HIDDEN_PROPERTIES.put(ICOFarmUser.CLASS_NAME, userProperties);
        HIDDEN_PROPERTIES.put("ORestricted", restrictedProperties);
        HIDDEN_PROPERTIES.put(OIdentity.CLASS_NAME, identityProperties);
        HIDDEN_PROPERTIES.put(Wallet.CLASS_NAME, walletProperties);
    }

    private void initHiddenWidgets() {
        HIDDEN_WIDGETS.put(REFERRAL, Collections.singletonList(LIST_DOCUMENTS_WIDGET_ID));
        HIDDEN_WIDGETS.put(Wallet.CLASS_NAME, Collections.singletonList(LIST_DOCUMENTS_WIDGET_ID));
    }

    private ODocument getOrCreatePerspective(String name, OSchemaHelper helper) {
        PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
        ODocument doc = perspectivesModule.getPerspectiveByName(helper.getDatabase(), name);
        if (doc == null) {
            doc = new ODocument(PerspectivesModule.OCLASS_PERSPECTIVE);
            doc.field("name", CommonUtils.toMap("en", name));
        }
        return doc;
    }

    private ODocument getOrCreatePerspectiveItem(String name, ODocument perspective, OSchemaHelper helper) {
        List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>("select from " + PerspectivesModule.OCLASS_ITEM
                + " where name.values() contains ? and perspective = ?"), name, perspective);
        ODocument doc = docs != null && !docs.isEmpty() ? docs.get(0) : null;
        if (doc == null) {
            doc = new ODocument(PerspectivesModule.OCLASS_ITEM);
            doc.field("name", CommonUtils.toMap("en", name));
        }
        return doc;
    }

    private void createWidgetIfNotExists(String typeId, String className, OSchemaHelper helper) {
        ODocument dashboard = getOrCreateDashboard(className, helper);
        if (!isWidgetExists(dashboard, typeId)) {
           ODocument doc = new ODocument(OCLASS_WIDGET);
           doc.field(OPROPERTY_TYPE_ID, typeId);
           doc.field(OPROPERTY_DASHBOARD, dashboard);
           doc.save();
        }
    }

    private ODocument getOrCreateDashboard(String className, OSchemaHelper helper) {
        String sql = String.format("select from %s where %s = ?", OCLASS_DASHBOARD, OPROPERTY_CLASS);
        List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>(sql, 1), className);
        ODocument doc;
        if (docs == null || docs.isEmpty()) {
            doc = new ODocument(OCLASS_DASHBOARD);
            doc.field(OPROPERTY_DOMAIN, "browse");
            doc.field(OPROPERTY_TAB, "list");
            doc.field(OPROPERTY_CLASS, className);
            doc.save();
        } else doc = docs.get(0);

        return doc;
    }

    private boolean isWidgetExists(ODocument dashboard, String typeId) {
        List<OIdentifiable> widgets = (List<OIdentifiable>) dashboard.field(OPROPERTY_WIDGETS);
        return widgets != null && !widgets.isEmpty() && widgets.stream()
                .anyMatch(widget -> typeId.equals(((ODocument)widget.getRecord()).field(OPROPERTY_TYPE_ID)));
    }

    private void updateRole(ODatabaseDocument db, String perspectiveName, String field, String roleName) {
        OSecurity security = db.getMetadata().getSecurity();
        PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
        ODocument perspective = perspectivesModule.getPerspectiveByName(db, perspectiveName);
        ORole role = security.getRole(roleName);
        role.getDocument().field("perspective", perspective);
        perspective.field(field, role.getDocument());
        role.save();
        perspective.save();
    }

    private void updateUser(ODatabaseDocument db, String perspectiveName, String field, String username) {
        OSecurity security = db.getMetadata().getSecurity();
        PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
        ODocument perspective = perspectivesModule.getPerspectiveByName(db, perspectiveName);
        OUser user = security.getUser(username);
        user.getDocument().field("perspective", perspective);
        perspective.field(field, user.getDocument());
        user.save();
        perspective.save();
    }
}
