package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.util.ICOFarmUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class UserHook extends ODocumentHookAbstract {

    private final List<String> protectedFields;

    public UserHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(ICOFarmUser.CLASS_NAME);

        protectedFields = new ArrayList<>(8);
        protectedFields.add(ICOFarmUser.OPROPERTY_ID);
        protectedFields.add(ICOFarmUser.OPROPERTY_RESTORE_ID);
        protectedFields.add(ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED);
        protectedFields.add("name");
        protectedFields.add("online");
        protectedFields.add("status");
        protectedFields.add("lastSessionId");
        protectedFields.add("roles");
        protectedFields.sort(Comparator.naturalOrder());
    }

    @Override
    public RESULT onRecordBeforeUpdate(ODocument doc) {
        OSecurityUser user = doc.getDatabase().getUser();
        if (!ICOFarmUtils.isAdmin(user)) {
            String[] dirtyFields = doc.getDirtyFields();
            for (String dirtyField : dirtyFields) {
                if (protectedFields.contains(dirtyField)) {
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
