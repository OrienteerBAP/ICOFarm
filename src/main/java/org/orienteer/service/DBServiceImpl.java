package org.orienteer.service;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduledEventBuilder;
import com.orientechnologies.orient.core.schedule.OScheduler;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.orienteer.ICOFarmApplication;
import org.orienteer.core.tasks.OTask;
import org.orienteer.core.tasks.OTaskSession;
import org.orienteer.model.*;
import org.orienteer.module.ICOFarmModule;
import org.orienteer.module.ICOFarmSecurityModule;
import org.orienteer.tasks.LoadTokenTransactionsTask;
import org.orienteer.util.ICOFarmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.orienteer.module.ICOFarmModule.*;

@Singleton
public class DBServiceImpl implements IDBService {

    private static final Logger LOG = LoggerFactory.getLogger(DBServiceImpl.class);

    private ThreadLocal<ICOFarmDBClosure> dbClosure = ThreadLocal.withInitial(ICOFarmDBClosure::new);

    @Override
    public ICOFarmUser getUserBy(String field, String value) {
        ODocument doc = getUserBy(null, field, value);
        return doc != null ? new ICOFarmUser(doc) : null;
    }

    @Override
    public List<ICOFarmUser> getUsers() {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME));
        return !isDocsNotEmpty(docs) ? Collections.emptyList() : docs.stream().map(ICOFarmUser::new).collect(Collectors.toList());
    }

    @Override
    public OFunction getFunctionByName(String name) {
        return getFunctionByName(null, name);
    }

    @Override
    public OMail getMailByName(String name) {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(String.format("select from %s where %s = ?",
                OMail.CLASS_NAME, OMail.NAME), 1), name);
        return getFromDocs(docs, OMail::new);
    }

    @Override
    public ORole getRoleByName(String name) {
        return getRoleByName(null, name);
    }

    @Override
    public OTransaction getTransactionByHash(String hash) {
        ODocument doc = getTransactionByHash(null, hash);
        return doc != null ? new OTransaction(doc) : null;
    }

    @Override
    public List<Wallet> getWallets() {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME));
        return !isDocsNotEmpty(docs) ? Collections.emptyList() : docs.stream().map(Wallet::new).collect(Collectors.toList());
    }

    @Override
    public List<Wallet> getUserWallets(ODocument userDoc) {
        String sql = String.format("select from %s where %s = ? order by %s", Wallet.CLASS_NAME, Wallet.OPROPERTY_OWNER,
                Wallet.OPROPERTY_CREATED);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql), userDoc);
        return !isDocsNotEmpty(docs) ? Collections.emptyList() : docs.stream().map(Wallet::new).collect(Collectors.toList());
    }

    @Override
    public List<Wallet> getUserWallets(ICOFarmUser user) {
        return getUserWallets(user.getDocument());
    }

    @Override
    public List<Token> getTokens(boolean allTokens) {
        String sql = allTokens ? "select from " + Token.CLASS_NAME : String.format("select from %s where %s != ?",
                Token.CLASS_NAME, Token.OPROPERTY_ADDRESS);
        return getTokens(null, sql, allTokens ? null : ICOFarmModule.ZERO_ADDRESS);
    }

    @Override
    public List<Token> getCurrencyTokens() {
        String sql = String.format("select from %s where %s = ?", Token.CLASS_NAME, Token.OPROPERTY_ADDRESS);
        return getTokens(null, sql, ICOFarmModule.ZERO_ADDRESS);
    }

    @Override
    public Token getTokenBySymbol(String symbol) {
        String sql = String.format("select from %s where %s = ?", Token.CLASS_NAME, Token.OPROPERTY_SYMBOL);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), symbol);
        return getFromDocs(docs, Token::new);
    }

    @Override
    public ICOFarmUser createInvestorUser(String email, String password, String firstName, String lastName, boolean active) {
        return (ICOFarmUser) dbClosure.get().execute(db -> {
            ORole role = getRoleByName(db, ICOFarmSecurityModule.INVESTOR_ROLE);
            ICOFarmUser user = new ICOFarmUser();
            user.setEmail(email)
                    .setId(UUID.randomUUID().toString())
                    .setFirstName(firstName)
                    .setLastName(lastName)
                    .setName(email)
                    .setPassword(password)
                    .addRole(role)
                    .setAccountStatus(active ? OSecurityUser.STATUSES.ACTIVE : OSecurityUser.STATUSES.SUSPENDED);
            user.save();
            return user;
        });
    }

    @Override
    public void updateReferralInformation(ICOFarmUser user, ICOFarmUser by) {
        dbClosure.get().execute((db) -> {
            ODocument doc = new ODocument(REFERRAL);
            doc.field(OPROPERTY_REFERRAL_CREATED, new Date());
            doc.field(OPROPERTY_REFERRAL_USER, user.getDocument());
            doc.field(OPROPERTY_REFERRAL_BY, by.getDocument());
            doc.field(ICOFarmSecurityModule.ORESTRICTED_ALLOW_READ, Collections.singletonList(by.getDocument()));
            return null;
        });
    }

    @Override
    public ICOFarmUser updateUserPassword(ICOFarmUser user, String password) {
        return (ICOFarmUser) dbClosure.get().execute(db -> user.setPassword(password).save());
    }

    @Override
    public ICOFarmUser updateUserStatus(ICOFarmUser user, boolean active) {
        return (ICOFarmUser) dbClosure.get().execute((db) -> {
            user.setActive(active);
            user.save();
            return user;
        });
    }

    @Override
    public ICOFarmUser createRestoreStatusForUser(ICOFarmUser user) {
        return (ICOFarmUser) dbClosure.get().execute((db) -> {
            user.setRestoreId(UUID.randomUUID().toString())
                    .setRestoreIdCreated(new Date());
            user.save();
            return user;
        });
    }

    @Override
    public void createRestorePasswordScheduler(ICOFarmUser user) {
        dbClosure.get().execute((db) -> {
            String name = EVENT_RESTORE_PASSWORD_PREFIX + user.getRestoreId();
            OScheduledEvent event = createRestorePasswordSchedulerEvent(db, user, name);
            OScheduler scheduler = db.getMetadata().getScheduler();
            scheduler.removeEvent(name);
            scheduler.scheduleEvent(event);
            return null;
        });
    }

    @Override
    public void clearRestoreStatusForUser(ICOFarmUser user) {
        dbClosure.get().execute(db -> {
            user.setRestoreId(null);
            user.setRestoreIdCreated(null);
            db.getMetadata().getScheduler().removeEvent(EVENT_RESTORE_PASSWORD_PREFIX + user.getRestoreId());
            user.save();
            return null;
        });
    }

    @Override
    public Wallet createWalletForUser(ICOFarmUser user) {
        return createWalletForUser(user, null, null, null);
    }

    @Override
    public Wallet createWalletForUser(ICOFarmUser user, String name, String address, byte[] json) {
        return (Wallet) dbClosure.get().execute(db -> {
            Wallet wallet = new Wallet();
            wallet.setName(name);
            wallet.setOwner(user.getDocument());
            wallet.setWalletJSON(json);
            wallet.setAddress(address);
            wallet.setCreated(new Date());
            wallet.setDisplayableToken(getTokenBySymbol(ICOFarmModule.ETH).getDocument());
            wallet.getDocument().field(ICOFarmSecurityModule.ORESTRICTED_ALLOW, Collections.singletonList(user.getDocument()));
            wallet.save();
            return wallet;
        });
    }

    @Override
    public Wallet getWalletByTransactionFromOrTo(ICOFarmUser owner, String from, String to) {
        return getWalletByTransactionFromOrTo(owner.getDocument(), from, to);
    }

    @Override
    public Wallet getWalletByTransactionFromOrTo(ODocument owner, String from, String to) {
        ODocument doc = getWalletByTransactionFromOrTo(null, owner, from, to);
        return doc != null ? new Wallet(doc) : null;
    }

    @Override
    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction) {
        dbClosure.get().execute(db -> {
            filterICOFarmTransactions(db, transactions)
                    .forEach(t -> {
                        LOG.info("save confirmed transaction: {} {}", t.getHash(), Thread.currentThread().getName()); //TODO: remove in next stable version
                        updateOrCreateConfirmedTransaction(db, t, blockFunction.apply(t));
                    });
            return null;
        });
    }

    @Override
    public void saveUnconfirmedICOFarmTransactions(List<Transaction> transactions) {
        dbClosure.get().execute(db -> {
            filterICOFarmTransactions(db, transactions)
                    .forEach(t -> {
                        LOG.info("save unconfirmed transaction: {} {}", t.getHash(), Thread.currentThread().getName()); //TODO: remove in next stable version
                        saveUnconfirmedTransaction(db, t);
                    });
            return null;
        });
    }

    @Override
    public OTransaction saveTransaction(Transaction transaction, EthBlock.Block block) {
        return (OTransaction) dbClosure.get().execute(db -> {
            ODocument doc;
            if (block != null) {
                doc = saveConfirmedTransaction(db, transaction, ICOFarmUtils.computeTimestamp(block));
            } else doc = saveUnconfirmedTransaction(db, transaction);

            return new OTransaction(doc);
        });
    }

    @Override
    public void saveTransactionsFromTransferEvents(List<TransferEvent> transferEvents) {
        dbClosure.get().execute(db -> {
            for (TransferEvent event : transferEvents) {
                LOG.info("save transfer tokens event: {} {}", event.toString(), Thread.currentThread().getName());
                ODocument doc = updateOrCreateConfirmedTransaction(db, event.getTransaction(), event.getBlock());
                doc.field(OTransaction.OPROPERTY_TOKENS, new BigDecimal(event.getTokens()));
                doc.save();
            }
            return null;
        });
    }

    @Override
    public LoadTokenTransactionsTask createLoadTokenTransactionsTask(Token token, DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        return (LoadTokenTransactionsTask) dbClosure.get().execute(db -> {
            LoadTokenTransactionsTask task = LoadTokenTransactionsTask.create(token, false);
            task.setStartBlock(startBlock);
            task.setEndBlock(endBlock);
            task.save();
            return task;
        });
    }

    @Override
    public LoadTokenTransactionsTask getLoadTokenTransactionsTask(Token token) {
        return getTaskByName(LoadTokenTransactionsTask.NAME_PREFIX + token.getSymbol(), LoadTokenTransactionsTask.class);
    }

    @Override
    public <T extends OTask> T getTaskByName(String name, Class<T> taskClass) {
        String sql = String.format("select from %s where %s = ?", OTask.TASK_CLASS, OTask.Field.NAME.fieldName());
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), name);
        return getFromDocs(docs, doc -> {
            try {
                return taskClass.getConstructor(ODocument.class).newInstance(doc);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    @Override
    public OTaskSession getRunningSessionForTask(OTask task) {
        String sql = String.format("select from %s where %s = ? and %s = ?", OTaskSession.TASK_SESSION_CLASS,
                OTaskSession.Field.TASK_LINK.fieldName(), OTaskSession.Field.STATUS);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), task.getDocument(), OTaskSession.Status.RUNNING.name());
        return getFromDocs(docs, OTaskSession::new);
    }

    @Override
    public BigInteger getTokenTransactionsCount(Token token) {
        String sql = String.format("select count(*) as count from %s where %s = ?", OTransaction.CLASS_NAME, OTransaction.OPROPERTY_TO);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), token.getAddress());
        Long count = isDocsNotEmpty(docs) ? docs.get(0).field("count") : null;
        return count != null ? new BigInteger(count.toString()) : BigInteger.ZERO;
    }

    @Override
    public BigInteger getSoldTokensCount(Token token) {
        String sql = String.format("select sum(%s) as sum from %s where %s = ?", OTransaction.OPROPERTY_TOKENS, OTransaction.CLASS_NAME, OTransaction.OPROPERTY_TO);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), token.getAddress());
        String sum = isDocsNotEmpty(docs) ? docs.get(0).field("sum").toString() : null;
        return !Strings.isNullOrEmpty(sum) ? new BigInteger(sum) : BigInteger.ZERO;
    }

    @Override
    public boolean isTokenAddress(String address) {
        String sql = String.format("select from %s where %s = ?", Token.CLASS_NAME, Token.OPROPERTY_ADDRESS);
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(sql, 1), address);
        return isDocsNotEmpty(docs);
    }

    @Override
    public void save(ODocumentWrapper documentWrapper) {
        save(documentWrapper.getDocument());
    }

    @Override
    public void save(ODocument doc) {
        dbClosure.get().execute(db -> db.save(doc));
    }

    private ODocument saveUnconfirmedTransaction(ODatabaseDocument database, Transaction transaction) {
        ODocument from = getUserByWalletAddress(database, transaction.getFrom());
        ODocument to = getUserByWalletAddress(database, transaction.getTo());
        Set<ODocument> readers = new HashSet<>();
        if (from != null) readers.add(from);
        if (to != null) readers.add(to);

        ODocument doc = createTransactionDocument(transaction, readers, false);
        if (database == null) {
            dbClosure.get().execute(db -> {
                doc.save();
                return null;
            });
        } else {
            doc.save();
        }
        return doc;
    }

    private ODocument updateOrCreateConfirmedTransaction(ODatabaseDocument db, Transaction transaction, EthBlock.Block block) {
        Date date = ICOFarmUtils.computeTimestamp(block);
        ODocument result = getTransactionByHash(db, transaction.getHash());
        if (result != null) {
            updateConfirmedTransaction(result, date, transaction).save();
        } else {
            result = saveConfirmedTransaction(db, transaction, date);
        }
        return result;
    }

    private ODocument saveConfirmedTransaction(ODatabaseDocument db, Transaction transaction, Date date) {
        ODocument doc = saveUnconfirmedTransaction(db, transaction);
        updateConfirmedTransaction(doc, date, transaction).save();
        return doc;
    }

    private Stream<Transaction> filterICOFarmTransactions(ODatabaseDocument db, List<Transaction> transactions) {
        return transactions.stream().filter(t -> isICOFarmTransaction(db, t));
    }

    private boolean isICOFarmTransaction(ODatabaseDocument db, Transaction transaction) {
        List<ODocument> docs = query(db, new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME + " where "
                + Wallet.OPROPERTY_ADDRESS + " = ? OR "
                + Wallet.OPROPERTY_ADDRESS + " = ?", 1), transaction.getFrom(), transaction.getTo());
        return isDocsNotEmpty(docs);
    }

    private ODocument getTransactionByHash(ODatabaseDocument db, String hash) {
        String sql = String.format("select from %s where %s = ?", OTransaction.CLASS_NAME, OTransaction.OPROPERTY_HASH);
        List<ODocument> docs = query(db, new OSQLSynchQuery<>(sql, 1), hash);
        return isDocsNotEmpty(docs) ? docs.get(0) : null;
    }

    private ODocument createTransactionDocument(Transaction transaction, Collection<ODocument> readers, boolean confirmed) {
        ODocument doc = new ODocument(OTransaction.CLASS_NAME);
        doc.field(OTransaction.OPROPERTY_FROM, transaction.getFrom().toLowerCase());
	    doc.field(OTransaction.OPROPERTY_TO, transaction.getTo().toLowerCase());
	    doc.field(OTransaction.OPROPERTY_HASH, transaction.getHash().toLowerCase());
	    doc.field(OTransaction.OPROPERTY_VALUE, transaction.getValue().toString());
        doc.field(OTransaction.OPROPERTY_CONFIRMED, confirmed);
        doc.field(ICOFarmSecurityModule.ORESTRICTED_ALLOW_READ, readers);
        return doc;
    }

    private ODocument getWalletByTransactionFromOrTo(ODatabaseDocument db, ODocument owner, String from, String to) {
        String sql = String.format("select from %s where %s = ? or %s = ? and %s = ?",
                Wallet.CLASS_NAME, Wallet.OPROPERTY_ADDRESS, Wallet.OPROPERTY_ADDRESS, Wallet.OPROPERTY_OWNER);
        List<ODocument> docs = query(db, new OSQLSynchQuery<>(sql, 1), from, to, owner);
        return isDocsNotEmpty(docs) ? docs.get(0) : null;
    }


    private ODocument updateConfirmedTransaction(ODocument doc, Date date, Transaction transaction) {
        doc.field(OTransaction.OPROPERTY_CONFIRMED, true);
        doc.field(OTransaction.OPROPERTY_BLOCK, transaction.getBlockNumber().toString());
        doc.field(OTransaction.OPROPERTY_TIMESTAMP, date);
        return doc;
    }

    private boolean isDocsNotEmpty(List<ODocument> docs) {
        return docs != null && !docs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<ODocument> query(ODatabaseDocument database, OSQLSynchQuery<ODocument> query, Object... args) {
        if (database != null) {
            return database.query(query, args);
        }
        return (List<ODocument>) dbClosure.get().execute((db) -> db.query(query, args));
    }

    private ODocument getUserByWalletAddress(ODatabaseDocument db, String address) {
        String sql = String.format("select %s from %s where %s = ?", Wallet.OPROPERTY_OWNER, Wallet.CLASS_NAME, Wallet.OPROPERTY_ADDRESS);
        List<ODocument> docs = query(db, new OSQLSynchQuery<>(sql, 1), address);
        return isDocsNotEmpty(docs) ? (ODocument) docs.get(0).field(Wallet.OPROPERTY_OWNER) : null;
    }

    private ODocument getUserBy(ODatabaseDocument db, String field, String value) {
        List<ODocument> docs = query(db, new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where "
                + field + " = ?", 1), value);
        return isDocsNotEmpty(docs) ? docs.get(0) : null;
    }

    private ORole getRoleByName(ODatabaseDocument database, String name) {
        if (database != null) {
            return database.getMetadata().getSecurity().getRole(name);
        }
        return (ORole) dbClosure.get().execute(db -> db.getMetadata().getSecurity().getRole(name));
    }

    private OFunction getFunctionByName(ODatabaseDocument db, String name) {
        List<ODocument> docs = query(db, new OSQLSynchQuery<>("select from " + OFunction.CLASS_NAME
                + " where name = ?", 1), name);
        return getFromDocs(docs, OFunction::new);
    }

    private List<Token> getTokens(ODatabaseDocument db, String sql, Object...args) {
        List<ODocument> docs = query(db, new OSQLSynchQuery<>(sql), args);
        return !isDocsNotEmpty(docs) ? Collections.emptyList() : docs.stream().map(Token::new).collect(Collectors.toList());
    }

    private <T> T getFromDocs(List<ODocument> docs, Function<ODocument, T> f) {
        return isDocsNotEmpty(docs) ? f.apply(docs.get(0)) : null;
    }

    private OScheduledEvent createRestorePasswordSchedulerEvent(ODatabaseDocument db, ICOFarmUser user, String name) {
        OProperty property = user.getDocument().getSchemaClass().getProperty(ICOFarmUser.OPROPERTY_RESTORE_ID);
        OFunction f = getFunctionByName(db, FUN_REMOVE_RESTORE_ID_BY_EMAIL);
        long timeout = Long.parseLong(ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.getValue(property));
        Map<Object, Object> args = new HashMap<>(2);
        args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL, user.getEmail());
        args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME, name);
        args.put(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT, timeout);
        return new OScheduledEventBuilder()
                .setName(name)
                .setFunction(f)
                .setArguments(args)
                .setRule(ICOFarmApplication.REMOVE_CRON_RULE.getValue(property))
                .setStartTime(new Date(System.currentTimeMillis() + timeout)).build();
    }

    private static class ICOFarmDBClosure extends DBClosure<Object> {
        private Function<ODatabaseDocument, Object> dbFunction;

        public ICOFarmDBClosure() {
            super();
        }

        public Object execute(Function<ODatabaseDocument, Object> dbFunction) {
            this.dbFunction = dbFunction;
            return super.execute();
        }

        @Override
        protected Object execute(ODatabaseDocument db) {
            Object result = dbFunction.apply(db);
            this.dbFunction = null;
            return result;
        }
    }
}