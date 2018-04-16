package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.Wallet;
import org.orienteer.service.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.Date;

public class WalletHook extends ODocumentHookAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(WalletHook.class);

    public WalletHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(Wallet.CLASS_NAME);
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);

        if (doc.field(Wallet.OPROPERTY_ADDRESS) == null) {
            createWallet(doc, service);
        } else checkIfAddressValid(doc, service);

        if (doc.field(Wallet.OPROPERTY_OWNER) == null) {
            doc.field(Wallet.OPROPERTY_OWNER, OrienteerWebSession.get().getUser().getDocument());
        }

        if (doc.field(Wallet.OPROPERTY_NAME) == null) {
            doc.field(Wallet.OPROPERTY_NAME, (String) doc.field(Wallet.OPROPERTY_ADDRESS));
        }

        doc.field(Wallet.OPROPERTY_CREATED, new Date());
        updateWalletBalance(doc, service);

        return super.onRecordBeforeCreate(doc);
    }

    @Override
    public RESULT onRecordBeforeUpdate(ODocument doc) {
        IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
        checkIfAddressValid(doc, service);

        return super.onRecordBeforeUpdate(doc);
    }

    private void createWallet(ODocument doc, IEthereumService service) {
        try {
            String password = OrienteerWebSession.get().getPassword();
            byte [] wallet = service.createWallet(password);
            Credentials credentials = service.readWallet(password, wallet);
            doc.field(Wallet.OPROPERTY_ADDRESS, credentials.getAddress());
            doc.field(Wallet.OPROPERTY_WALLET_JSON, wallet);
        } catch (Exception e) {
            LOG.error("Can't create new wallet: {}", doc, e);
        }
    }

    private void updateWalletBalance(ODocument doc, IEthereumService service) {
        try {
            BigInteger balance = service.requestBalance(doc.field(Wallet.OPROPERTY_ADDRESS));
            if (balance != null) doc.field(Wallet.OPROPERTY_BALANCE, balance.toString());
        } catch (Exception e) {}
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
