package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.model.*;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.util.List;
import java.util.function.Function;

@ImplementedBy(DBServiceImpl.class)
public interface IDBService extends IClusterable {

    public ICOFarmUser getUserBy(String field, String value);

    public List<ICOFarmUser> getUsers();

    public OFunction getFunctionByName(String name);

    public OMail getMailByName(String name);

    public ORole getRoleByName(String name);

    public List<Wallet> getWallets();

    public List<Wallet> getUserWallets(ODocument userDoc);
    public List<Wallet> getUserWallets(ICOFarmUser user);

    public List<Token> getTokens();

    public ICOFarmUser createInvestorUser(String email, String password, String firstName, String lastName, boolean active);

    public void updateReferralInformation(ICOFarmUser user, ICOFarmUser by);

    public ICOFarmUser updateUserPassword(ICOFarmUser user, String password);

    public ICOFarmUser updateUserStatus(ICOFarmUser user, boolean active);

    public ICOFarmUser createRestoreStatusForUser(ICOFarmUser user);

    public void createRestorePasswordScheduler(ICOFarmUser user);

    public void clearRestoreStatusForUser(ICOFarmUser user);

    public Wallet createWalletForUser(ICOFarmUser user);

    public Wallet getWalletByTransactionFromOrTo(ICOFarmUser owner, String from, String to);
    public Wallet getWalletByTransactionFromOrTo(ODocument owner, String from, String to);

    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction);
    public void saveUnconfirmedICOFarmTransactions(List<Transaction> transactions);

}
