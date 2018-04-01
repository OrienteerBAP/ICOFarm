package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.ICOFarmModule;
import org.orienteer.model.ICOFarmUser;

/**
 * Hook which will skip OUser documents which is not documents for current user.
 * Works for user with role {@link ICOFarmModule#INVESTOR_ROLE}
 */
public class ICOFarmOUserHook extends ODocumentHookAbstract {

    public ICOFarmOUserHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(ICOFarmUser.CLASS_NAME);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

    @Override
    public RESULT onRecordBeforeRead(ODocument doc) {
        OSecurityUser user = doc.getDatabase().getUser();
        if (user.hasRole(ICOFarmModule.INVESTOR_ROLE, true)) {
            String docName = doc.field("name");
            return user.getName().equals(docName) ? RESULT.RECORD_NOT_CHANGED : RESULT.SKIP;
        }
        return RESULT.RECORD_NOT_CHANGED;
    }
}
