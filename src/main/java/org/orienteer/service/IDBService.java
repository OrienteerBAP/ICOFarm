package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.core.tasks.OTask;
import org.orienteer.core.tasks.OTaskSession;
import org.orienteer.model.*;
import org.orienteer.tasks.LoadTokenTransactionsTask;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import java.math.BigInteger;
import java.util.List;
import java.util.function.Function;

@ImplementedBy(DBServiceImpl.class)
public interface IDBService extends IClusterable {

    public ICOFarmUser getUserBy(String field, String value);

    public List<ICOFarmUser> getUsers();

    public OFunction getFunctionByName(String name);

    public OMail getMailByName(String name);

    public ORole getRoleByName(String name);

    public OTransaction getTransactionByHash(String hash);

    public List<Wallet> getWallets();

    public List<Wallet> getUserWallets(ODocument userDoc);
    public List<Wallet> getUserWallets(ICOFarmUser user);

    public List<Token> getTokens(boolean allTokens);

    public List<Token> getCurrencyTokens();

    public Token getTokenBySymbol(String symbol);
    public Token getTokenByAddress(String address);

    public ICOFarmUser createInvestorUser(String email, String password, String firstName, String lastName, boolean active);

    public void updateReferralInformation(ICOFarmUser user, ICOFarmUser by);

    public ICOFarmUser updateUserPassword(ICOFarmUser user, String password);

    public ICOFarmUser updateUserStatus(ICOFarmUser user, boolean active);

    public ICOFarmUser createRestoreStatusForUser(ICOFarmUser user);

    public void createRestorePasswordScheduler(ICOFarmUser user);

    public void clearRestoreStatusForUser(ICOFarmUser user);

    public Wallet createWalletForUser(ICOFarmUser user);
    public Wallet createWalletForUser(ICOFarmUser user, String name, String address, byte[] json);

    public Wallet getWalletByTransactionFromOrTo(ICOFarmUser owner, String from, String to);
    public Wallet getWalletByTransactionFromOrTo(ODocument owner, String from, String to);

    public List<Wallet> getWalletsByAddress(String address);

    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction);
    public void saveUnconfirmedICOFarmTransactions(List<Transaction> transactions);

    public OTransaction saveTransaction(Transaction transaction, EthBlock.Block block);

    public void saveTransactionsFromTransferEvents(List<TransferEvent> transferEvents);

    public LoadTokenTransactionsTask createLoadTokenTransactionsTask(Token token, DefaultBlockParameter startBlock, DefaultBlockParameter endBlock);

    public LoadTokenTransactionsTask getLoadTokenTransactionsTask(Token token);

    public <T extends OTask> T getTaskByName(String name, Class<T> taskClass);

    public OTaskSession getRunningSessionForTask(OTask task);

    public BigInteger getTokenTransactionsCount(Token token);
    public BigInteger getSoldTokensCount(Token token);

    public boolean isTokenAddress(String address);

    public void save(ODocumentWrapper...documentWrapper);
    public void save(ODocument...doc);

}
