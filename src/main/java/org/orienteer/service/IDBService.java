package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.model.EmbeddedWallet;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;
import org.orienteer.model.Wallet;
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

    public List<Wallet> getUserWallets(ICOFarmUser user);

    public ICOFarmUser createInvestorUser(String email, String password, String firstName, String lastName, boolean active);

    public void updateReferralInformation(ICOFarmUser user, ICOFarmUser by);

    public ICOFarmUser updateUserPassword(ICOFarmUser user, String password);

    public ICOFarmUser updateUserStatus(ICOFarmUser user, boolean active);

    public ICOFarmUser createRestoreStatusForUser(ICOFarmUser user);

    public void createRestorePasswordScheduler(ICOFarmUser user);

    public void cleareRestoreStatusForUser(ICOFarmUser user);

    public EmbeddedWallet createEmbeddedWalletForUser(ICOFarmUser user);

    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction);
    public void saveUnconfirmedICOFarmTransactions(List<Transaction> transactions);

}
