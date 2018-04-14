package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.Wallet;
import org.orienteer.service.IEthereumService;

import java.math.BigInteger;
import java.util.Date;

public class WalletHook extends ODocumentHookAbstract {

    public WalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(Wallet.CLASS_NAME);
    }

    @Override
    public void onRecordAfterCreate(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
        if (doc.field(Wallet.OPROPERTY_OWNER) == null) {
            doc.field(Wallet.OPROPERTY_OWNER, OrienteerWebSession.get().getUser().getDocument());
        }
        doc.field(Wallet.OPROPERTY_CREATED, new Date());
        updateWalletBalance(doc, service);
    }

    private void updateWalletBalance(ODocument doc, IEthereumService service) {
        try {
            BigInteger balance = service.requestBalance(doc.field(Wallet.OPROPERTY_ADDRESS));
            if (balance != null) doc.field(Wallet.OPROPERTY_BALANCE, balance.toString());
        } catch (Exception e) {}
    }


    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
