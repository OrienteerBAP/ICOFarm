package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.module.ICOFarmPerspectiveModule;
import org.orienteer.util.ICOFarmUtils;

import java.util.List;

public class UserHook extends ODocumentHookAbstract {

    public UserHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(ICOFarmUser.CLASS_NAME);
    }

    @Override
    public RESULT onRecordBeforeUpdate(ODocument doc) {
        OSecurityUser user = doc.getDatabase().getUser();
        if (!ICOFarmUtils.isAdmin(user)) {
            String[] dirtyFields = doc.getDirtyFields();
            List<String> fields = ICOFarmPerspectiveModule.HIDDEN_PROPERTIES.get(ICOFarmUser.CLASS_NAME);
            for (String dirtyField : dirtyFields) {
                if (fields.contains(dirtyField)) {
                    throw new OSecurityAccessException("User " + user.getName() + " doesn't have permissions for update field: " + dirtyField);
                }
            }
        }
        return super.onRecordBeforeUpdate(doc);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
