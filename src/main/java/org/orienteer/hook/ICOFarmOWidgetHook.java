package org.orienteer.hook;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.util.ICOFarmUtils;

import java.util.List;

public class ICOFarmOWidgetHook extends ODocumentHookAbstract {

    public ICOFarmOWidgetHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(OWidgetsModule.OCLASS_WIDGET);
    }

    @Override
    public RESULT onRecordBeforeRead(ODocument doc) {
        String id = doc.field(OWidgetsModule.OPROPERTY_TYPE_ID);
        OSecurityUser user = doc.getDatabase().getUser();
        if (!Strings.isNullOrEmpty(id) && !ICOFarmUtils.isAdmin(user)) {
            if (id.equals(ICOFarmModule.SCHEMA_CLASSES_WIDGET_ID)) {
                throw new OSecurityAccessException("User " + user.getName() + " haven't access to schema!");
            } else {
                ODocument dashboard = doc.field(OWidgetsModule.OPROPERTY_DASHBOARD);
                String className = dashboard.field(OWidgetsModule.OPROPERTY_CLASS);
                List<String> hiddenWidgets = ICOFarmModule.HIDDEN_WIDGETS.get(className);
                if (hiddenWidgets != null && hiddenWidgets.contains(id)) {
                    return RESULT.SKIP;
                }
            }
        }
        return RESULT.RECORD_NOT_CHANGED;
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

}
