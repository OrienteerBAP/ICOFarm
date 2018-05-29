package org.orienteer.hook;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.exception.OValidationException;
import com.orientechnologies.orient.core.hook.ODocumentHookAbstract;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.service.web3.IICOFarmSmartContract;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TokenHook extends ODocumentHookAbstract {

    public TokenHook(ODatabaseDocument database) {
        super(database);
        setIncludeClasses(Token.CLASS_NAME);
    }

    @Override
    public RESULT onRecordBeforeCreate(ODocument doc) {
        onUpdate(doc);
        return super.onRecordBeforeCreate(doc);
    }

    @Override
    public RESULT onRecordBeforeUpdate(ODocument doc) {
        onUpdate(doc);
        return super.onRecordBeforeUpdate(doc);
    }

    private void onUpdate(ODocument token) {
        ODocument owner = token.field(Token.OPROPERTY_OWNER);
        String address = token.field(Token.OPROPERTY_ADDRESS);
        token.field(Token.OPROPERTY_ADDRESS, address.toLowerCase());
        if (owner != null) {
            OrienteerWebApplication app = OrienteerWebApplication.lookupApplication();
            IEthereumService ethService = app.getServiceInstance(IEthereumService.class);
            IICOFarmSmartContract contract = loadContract(owner, token, ethService);

            checkContract(contract);
            BigInteger weiCost = contract.getBuyPrice().toBlocking().value();
            checkWeiCost(weiCost);

            token.field(Token.OPROPERTY_ETHER_COST, Convert.fromWei(new BigDecimal(weiCost), Convert.Unit.ETHER));
        }
    }


    private void checkContract(IICOFarmSmartContract contract) {
        if (contract == null) {
            throw new OValidationException("Can't load contract with given address!");
        }
    }

    private void checkWeiCost(BigInteger weiCost) {
        if (weiCost.compareTo(BigInteger.ZERO) <= 0) {
            throw new OValidationException("Bad contract: weiCost must be more than 0!");
        }
    }

    private IICOFarmSmartContract loadContract(ODocument owner, ODocument token, IEthereumService ethService) {
        try {
            String address = token.field(Token.OPROPERTY_ADDRESS);
            return ethService.loadSmartContract(owner.field(Wallet.OPROPERTY_ADDRESS), address);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public DISTRIBUTED_EXECUTION_MODE getDistributedExecutionMode() {
        return DISTRIBUTED_EXECUTION_MODE.SOURCE_NODE;
    }
}
