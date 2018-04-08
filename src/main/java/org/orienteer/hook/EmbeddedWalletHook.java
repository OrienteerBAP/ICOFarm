package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.ICOFarmModule;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.OEmbeddedOWallet;
import org.orienteer.service.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

import java.util.UUID;

public class EmbeddedWalletHook extends ODocumentHookAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedWalletHook.class);


    public EmbeddedWalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(ICOFarmModule.EMBEDDED_WALLET);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
        String password = UUID.randomUUID().toString();
        try {
            String fileName = service.createWallet(password);
            Credentials credentials = service.requestWallet(password, fileName);
            doc.field(OEmbeddedOWallet.OPROPERTY_NAME, fileName);
            doc.field(OEmbeddedOWallet.OPROPERTY_PASSWORD, password);
            doc.field(OEmbeddedOWallet.OPROPERTY_ADDRESS, credentials.getAddress());
            return super.onRecordBeforeCreate(doc);
        } catch (Exception e) {
            LOG.error("Can't create new wallet: {}", doc, e);
        }
        return RESULT.SKIP;
    }

//    @Override
//    public void onRecordAfterCreate(ODocument doc) {
//        IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
//        String password = doc.field(OEmbeddedOWallet.OPROPERTY_PASSWORD);
//        String fileName = doc.field(OEmbeddedOWallet.OPROPERTY_NAME);
//        service.requestWalletAsync(password, fileName, (err, credentials) -> {
//            if (credentials != null) {
//                DBClosure.sudoConsumer(db -> {
//                    doc.field(OEmbeddedOWallet.OPROPERTY_BALANCE, )
//                });
//            }
//        });
//    }
}
