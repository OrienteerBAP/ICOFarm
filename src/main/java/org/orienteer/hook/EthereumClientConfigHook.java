package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.service.IEthereumUpdateService;

public class EthereumClientConfigHook extends ODocumentHookAbstract {

    public EthereumClientConfigHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(EthereumClientConfig.CLASS_NAME);
    }

    @Override
    public void onRecordAfterCreate(ODocument doc) {
        onRecordAfterUpdate(doc);
    }

    @Override
    public void onRecordAfterUpdate(ODocument doc) {
        IEthereumUpdateService service = OrienteerWebApplication.get().getServiceInstance(IEthereumUpdateService.class);
        service.destroy();
        service.init();
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
