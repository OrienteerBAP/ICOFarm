package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.CustomAttribute;
import org.orienteer.util.ICOFarmUtils;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Arrays;
import java.util.function.Consumer;

public class RestrictedODocumentHook extends ODocumentHookAbstract {

    public RestrictedODocumentHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses("ORestricted");
    }

    @Override
    public RESULT onRecordBeforeRead(ODocument doc) {
        OSecurityUser user = doc.getDatabase().getUser();
        switchCustomAttributes(doc, ICOFarmUtils.isAdmin(user));
        return super.onRecordBeforeRead(doc);
    }

    private void switchCustomAttributes(ODocument doc, boolean show) {
        boolean hidden = !show;
        String [] properties = { "_allow", "_allowRead", "_allowUpdate", "_allowDelete" };
        String tab = show ? "security" : null;

        Consumer<Consumer<OProperty>> updateProperty = (consumer) -> new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                OClass oClass = doc.getSchemaClass();
                if (db.getTransaction().isActive()) db.commit();

                Arrays.stream(properties).map(oClass::getProperty).forEach(consumer);
                return null;
            }
        }.execute();

        updateProperty.accept(p -> CustomAttribute.HIDDEN.setValue(p, hidden));
        updateProperty.accept(p -> CustomAttribute.TAB.setValue(p, tab));
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.BOTH;
    }
}
