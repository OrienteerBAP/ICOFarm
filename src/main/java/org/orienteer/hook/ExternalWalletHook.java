package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.ExternalWallet;
import org.orienteer.model.Wallet;
import org.orienteer.service.IEthereumService;

public class ExternalWalletHook extends ODocumentHookAbstract {

    public ExternalWalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(ExternalWallet.CLASS_NAME);
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        prepareWallet(doc);
        return super.onRecordBeforeCreate(doc);
    }

    @Override
    public RESULT onRecordBeforeUpdate(ODocument doc) {
        prepareWallet(doc);
        return super.onRecordBeforeUpdate(doc);
    }

    private void prepareWallet(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.lookupApplication().getServiceInstance(IEthereumService.class);
        checkIfAddressValid(doc, service);
    }

    private void checkIfAddressValid(ODocument doc, IEthereumService service) {
        String address = doc.field(Wallet.OPROPERTY_ADDRESS);
        if (!service.isAddressValid(address)) {
            throw new OValidationException(String.format("Account with address '%s' doesn't valid!", address));
        }
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
