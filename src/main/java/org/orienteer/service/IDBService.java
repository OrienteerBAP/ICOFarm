package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.util.io.IClusterable;
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

    public boolean isICOFarmTransaction(Transaction transaction);
    public void confirmTransaction(Transaction transaction, EthBlock.Block block);
    public ODocument saveUnconfirmedTransaction(Transaction transaction);

    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction);
    public void saveUnconfirmedICOFarmTransactions(List<Transaction> transactions);

}
