package org.orienteer.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.ICOFarmApplication;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;

public class ICOFarmSecurityModule extends AbstractOrienteerModule {

    public static final String INVESTOR_ROLE = "investor";

    public static final String ORESTRICTED_ALLOW        = "_allow";
    public static final String ORESTRICTED_ALLOW_READ   = "_allowRead";
    public static final String ORESTRICTED_ALLOW_UPDATE = "_allowUpdate";
    public static final String ORESTRICTED_ALLOW_DELETE = "_allowDelete";


    protected ICOFarmSecurityModule() {
        super("icofarm-security", ICOFarmModule.VERSION);
    }

    @Override
    public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
        adjustOUserClass(OSchemaHelper.bind(db));
        updateClassRestrictions(db);
        updateReaderPermissions(db);
        updateInvestorPermissions(db);
        return null;
    }

    @Override
    public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
        onInstall(app, db);
    }

    private void adjustOUserClass(OSchemaHelper helper) {
        updateDefaultOrientDbUsers(helper.getDatabase());

        OClass user = helper.oClass(ICOFarmUser.CLASS_NAME)
                .oProperty(ICOFarmUser.OPROPERTY_FIRST_NAME, OType.STRING, 0)
                .oProperty(ICOFarmUser.OPROPERTY_LAST_NAME, OType.STRING, 10)
                .oProperty(ICOFarmUser.OPROPERTY_EMAIL, OType.STRING, 20).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE).markAsDocumentName()
                .oProperty(ICOFarmUser.OPROPERTY_ID, OType.STRING).oIndex(OClass.INDEX_TYPE.UNIQUE).notNull()
                .oProperty(ICOFarmUser.OPROPERTY_RESTORE_ID, OType.STRING).switchDisplayable(false)
                .oProperty(ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED, OType.DATETIME).switchDisplayable(false).getOClass();

        updateUserCustomAttributes(user);

    }

    private void updateDefaultOrientDbUsers(ODatabaseDocument db) {
        OSecurity security = db.getMetadata().getSecurity();

        updateUserDocument(security.getUser("admin").getDocument());
        updateUserDocument(security.getUser("reader").getDocument());
        updateUserDocument(security.getUser("writer").getDocument());
    }

    private void updateUserDocument(ODocument doc) {
        doc.field(ICOFarmUser.OPROPERTY_EMAIL, UUID.randomUUID().toString() + "@gmail.com");
        doc.field(ICOFarmUser.OPROPERTY_ID, UUID.randomUUID().toString());
        doc.field(ORESTRICTED_ALLOW_READ, Collections.singleton(doc));
        doc.save();
    }

    private void updateUserCustomAttributes(OClass user) {
        ICOFarmApplication.REMOVE_CRON_RULE.setValue(user.getProperty(ICOFarmUser.OPROPERTY_RESTORE_ID), "0 0/1 * * * ?");
        ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.setValue(user.getProperty(ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED), "86400000");
        CustomAttribute.ORDER.setValue(user.getProperty("locale"), "40");
    }

    private void updateInvestorPermissions(ODatabaseDocument db) {
        OSecurity security = db.getMetadata().getSecurity();

        ORole investor = security.getRole(INVESTOR_ROLE) != null ? security.getRole(INVESTOR_ROLE) :
                security.createRole(INVESTOR_ROLE, OSecurityRole.ALLOW_MODES.DENY_ALL_BUT).setParentRole(security.getRole("reader"));

        investor.grant(ResourceGeneric.CLASS, OTransaction.CLASS_NAME, 15);

        investor.grant(ResourceGeneric.CLASS, Wallet.CLASS_NAME, 15);

        investor.grant(ResourceGeneric.CLASS, Token.CLASS_NAME, 2);

        investor.grant(ResourceGeneric.CLASS, ICOFarmModule.REFERRAL, 2);
        investor.grant(ResourceGeneric.CLASS, OUser.CLASS_NAME, 6);
        investor.grant(ResourceGeneric.CLASS, ICOFarmModule.REGISTRATION, 0);

        investor.grant(ResourceGeneric.CLASS, ICOFarmModule.BUY_TOKENS, 2);
        investor.grant(ResourceGeneric.CLASS, ICOFarmModule.TRANSFER_TOKENS, 2);

        investor.grant(ResourceGeneric.CLUSTER, "*", 15);

        investor.save();
    }

    private void updateReaderPermissions(ODatabaseDocument db) {
        ORole reader = db.getMetadata().getSecurity().getRole("reader");
        reader.grant(ResourceGeneric.CLASS, null, 0);
        reader.grant(ResourceGeneric.CLASS, ORole.CLASS_NAME, 0);
        reader.grant(ResourceGeneric.CLASS, ICOFarmModule.REGISTRATION, 2);
        reader.grant(ResourceGeneric.CLASS, OWidgetsModule.OCLASS_WIDGET, 2);
        reader.grant(ResourceGeneric.CLASS, OWidgetsModule.OCLASS_DASHBOARD, 2);
        reader.grant(ResourceGeneric.CLASS, PerspectivesModule.OCLASS_ITEM, 2);
        reader.grant(ResourceGeneric.CLASS, PerspectivesModule.OCLASS_PERSPECTIVE, 2);
        reader.save();
    }

    private void updateClassRestrictions(ODatabaseDocument db) {
        OSchema schema = db.getMetadata().getSchema();
        OClass restricted = schema.getClass("ORestricted");

        setRestricted(restricted, schema.getClass(OTransaction.CLASS_NAME));
        setRestricted(restricted, schema.getClass(ICOFarmModule.REFERRAL));
        setRestricted(restricted, schema.getClass(Wallet.CLASS_NAME));
        setRestricted(restricted, schema.getClass(OUser.CLASS_NAME));
    }

    private void setRestricted(OClass restricted, OClass oClass) {
        if (!oClass.isSubClassOf(restricted)) {
            oClass.addSuperClass(restricted);
            Collection<OProperty> properties = restricted.properties();
            oClass.properties().stream().filter(p -> !properties.contains(p))
                    .filter(p -> !(boolean) CustomAttribute.HIDDEN.getValue(p))
                    .forEach(p -> CustomAttribute.DISPLAYABLE.setValue(p, true));
        }
    }
}
