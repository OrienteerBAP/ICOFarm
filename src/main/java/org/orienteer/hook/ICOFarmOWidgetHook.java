package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.util.ICOFarmUtils;

public class ICOFarmOWidgetHook extends ODocumentHookAbstract {

    public ICOFarmOWidgetHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(OWidgetsModule.OCLASS_WIDGET);
    }

    @Override
    public RESULT onRecordBeforeRead(ODocument doc) {
        String id = doc.field(OWidgetsModule.OPROPERTY_TYPE_ID);
        OSecurityUser user = doc.getDatabase().getUser();
        if (id != null && id.equals("list-oclasses") && !ICOFarmUtils.isAdmin(user)) {
            throw new OSecurityAccessException("User " + user.getName() + " haven't access to schema!");
        }
        return RESULT.RECORD_NOT_CHANGED;
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

}
