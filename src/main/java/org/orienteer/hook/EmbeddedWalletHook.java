package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.EmbeddedWallet;
import org.orienteer.model.Wallet;
import org.orienteer.service.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

public class EmbeddedWalletHook extends ODocumentHookAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedWalletHook.class);


    public EmbeddedWalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(EmbeddedWallet.CLASS_NAME);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.lookupApplication().getServiceInstance(IEthereumService.class);
        try {
            String password = OrienteerWebSession.get().getPassword();
            byte [] wallet = service.createWallet(password);
            Credentials credentials = service.readWallet(password, wallet);
            doc.field(EmbeddedWallet.OPROPERTY_NAME, credentials.getAddress());
            doc.field(EmbeddedWallet.OPROPERTY_ADDRESS, credentials.getAddress());
            doc.field(Wallet.OPROPERTY_WALLET_JSON, wallet);
            return super.onRecordBeforeCreate(doc);
        } catch (Exception e) {
            LOG.error("Can't create new wallet: {}", doc, e);
        }
        return RESULT.SKIP;
    }
}
