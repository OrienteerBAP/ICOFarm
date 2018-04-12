package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.Wallet;

public class WalletHook extends ODocumentHookAbstract {

    public WalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(Wallet.CLASS_NAME);
    }

    @Override
    public void onRecordAfterCreate(ODocument doc) {
        if (doc.field(Wallet.OPROPERTY_OWNER) == null) {
            doc.field(Wallet.OPROPERTY_OWNER, OrienteerWebSession.get().getUser().getDocument());
        }
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
