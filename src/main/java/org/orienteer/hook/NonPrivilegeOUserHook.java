package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.util.ICOFarmUtils;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

public class NonPrivilegeOUserHook extends ODocumentHookAbstract {

    public NonPrivilegeOUserHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(OUser.CLASS_NAME);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

    @Override
    public RESULT onRecordBeforeRead(ODocument doc) {
        boolean isAdmin = ICOFarmUtils.isAdmin(doc.getDatabase().getUser());
        switchVisibility(doc.getSchemaClass(), !isAdmin);
        return super.onRecordBeforeRead(doc);
    }

    private void switchVisibility(OClass user, boolean hide) {
        new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                updateVisibility(user.getProperty(ICOFarmUser.ID), hide);
                updateVisibility(user.getProperty(ICOFarmUser.RESTORE_ID), hide);
                updateVisibility(user.getProperty(ICOFarmUser.RESTORE_ID_CREATED), hide);
                updateVisibility(user.getProperty("name"), hide);
                updateVisibility(user.getProperty("online"), hide);
                updateVisibility(user.getProperty("status"), hide);
                updateVisibility(user.getProperty("perspective"), hide);
                updateVisibility(user.getProperty("perspectiveItem"), hide);
                updateVisibility(user.getProperty("lastSessionId"), hide);
                updateVisibility(user.getProperty("roles"), hide);
                return null;
            }

            private void updateVisibility(OProperty property, boolean hide) {
                if (property != null) {
                    CustomAttribute.HIDDEN.setValue(property, hide);
                    CustomAttribute.DISPLAYABLE.setValue(property, !hide);
                }
            }
        }.execute();
    }
}
