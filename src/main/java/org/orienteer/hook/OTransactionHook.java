package org.orienteer.hook;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.OTransaction;
import org.orienteer.service.IEthereumService;
import org.orienteer.util.ICOFarmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

public class OTransactionHook extends ODocumentHookAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(OTransactionHook.class);

    public OTransactionHook(ODatabaseDocument db) {
        super(db);
        setIncludeClasses(OTransaction.CLASS_NAME);
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        if (doc.field(OTransaction.OPROPERTY_FROM) == null) {
            loadTransaction(doc);
        }
        return super.onRecordBeforeCreate(doc);
    }

    private void loadTransaction(ODocument doc) {
        String hash = doc.field(OTransaction.OPROPERTY_HASH);
        if (!Strings.isNullOrEmpty(hash)) {
            IEthereumService service = OrienteerWebApplication.get().getServiceInstance(IEthereumService.class);
            try {
                Transaction transaction = service.requestTransactionByHash(hash);
                if (transaction != null) {
                    EthBlock block = service.requestBlock(transaction.getBlockNumberRaw());
                    doc.field(OTransaction.OPROPERTY_FROM, transaction.getFrom());
                    doc.field(OTransaction.OPROPERTY_TO, transaction.getTo());
                    doc.field(OTransaction.OPROPERTY_VALUE, transaction.getValue().toString());
                    doc.field(OTransaction.OPROPERTY_BLOCK, transaction.getBlockNumber().toString());
                    doc.field(OTransaction.OPROPERTY_TIMESTAMP, ICOFarmUtils.computeTimestamp(block.getBlock()));
                    doc.field(OTransaction.OPROPERTY_CONFIRMED, true);
                    return;
                }
            } catch (Exception ex) {
                LOG.error("Can't get information about transaction: {}", doc, ex);
            }
        }
        throw new IllegalStateException("Can't create transaction: " + doc);
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
