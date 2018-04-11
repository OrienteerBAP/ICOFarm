package org.orienteer.service;

import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Wallet;
import org.orienteer.module.ICOFarmSecurityModule;
import org.orienteer.util.ICOFarmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class DBServiceImpl implements IDBService {

    private static final Logger LOG = LoggerFactory.getLogger(DBServiceImpl.class);

    private ThreadLocal<ICOFarmDBClosure> dbClosure = ThreadLocal.withInitial(ICOFarmDBClosure::new);

    @Override
    public ICOFarmUser getUserBy(String field, String value) {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where "
                + field + " = ?", 1), value);
        return getFromDocs(docs, ICOFarmUser::new);
    }

    @Override
    public List<ICOFarmUser> getUsers() {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME));
        return !isDocsNotEmpty(docs) ? Collections.emptyList() : docs.stream().map(ICOFarmUser::new).collect(Collectors.toList());
    }

    @Override
    public OFunction getFunctionByName(String name) {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + OFunction.CLASS_NAME
                + " where name = ?", 1), name);
        return getFromDocs(docs, OFunction::new);
    }

    @Override
    public OMail getMailByName(String name) {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>(String.format("select from %s where %s = ?",
                OMail.CLASS_NAME, OMail.NAME), 1), name);
        return getFromDocs(docs, OMail::new);
    }

    @Override
    public ORole getRoleByName(String name) {
        return (ORole) dbClosure.get().execute(db -> db.getMetadata().getSecurity().getRole(name));
    }

    @Override
    public List<Wallet> getWallets() {
        List<ODocument> docs = query(null, new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME));
        return docs == null || docs.isEmpty() ? Collections.emptyList() : docs.stream().map(Wallet::new)
                .collect(Collectors.toList());
    }

    @Override
    public void confirmICOFarmTransactions(List<Transaction> transactions, Function<Transaction, EthBlock.Block> blockFunction) {
        dbClosure.get().execute(db -> {
            filterICOFarmTransactions(db, transactions)
                    .forEach(t -> {
                        LOG.info("save confirmed transaction: {} {}", t.getHash(), Thread.currentThread().getName()); //TODO: remove in next stable version
                        saveConfirmedTransaction(db, t, blockFunction.apply(t));
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

    private ODocument saveUnconfirmedTransaction(ODatabaseDocument database, Transaction transaction) {
        ODocument from = getUserByWalletAddress(database, transaction.getFrom());
        ODocument to = from == null ? getUserByWalletAddress(database, transaction.getTo()) : null;
        ODocument result = createTransactionDocument(transaction, from != null ? from : to, false);

        if (database == null) {
            dbClosure.get().execute(db -> {
                result.save();
                return null;
            });
        } else result.save();

        return result;
    }

    private void saveConfirmedTransaction(ODatabaseDocument db, Transaction transaction, EthBlock.Block block) {
        String sql = String.format("select from %s where %s = ?", OTransaction.CLASS_NAME, OTransaction.OPROPERTY_HASH);
        List<ODocument> docs = db.query(new OSQLSynchQuery<>(sql), transaction.getHash());
        Date date = ICOFarmUtils.computeTimestamp(block);
        if (isDocsNotEmpty(docs)) {
            docs.forEach(doc -> updateConfirmedTransaction(doc, date, block).save());
        } else {
            ODocument doc = saveUnconfirmedTransaction(db, transaction);
            updateConfirmedTransaction(doc, date, block).save();
        }
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

    private ODocument createTransactionDocument(Transaction transaction, ODocument owner, boolean confirmed) {
        ODocument doc = new ODocument(OTransaction.CLASS_NAME);
        doc.field(OTransaction.OPROPERTY_FROM, transaction.getFrom());
	    doc.field(OTransaction.OPROPERTY_TO, transaction.getTo());
	    doc.field(OTransaction.OPROPERTY_HASH, transaction.getHash());
	    doc.field(OTransaction.OPROPERTY_VALUE, transaction.getValue().toString());
        doc.field(OTransaction.OPROPERTY_CONFIRMED, confirmed);
	    doc.field(ICOFarmSecurityModule.ORESTRICTED_ALLOW, Collections.singleton(owner));
        return doc;
    }

    private ODocument updateConfirmedTransaction(ODocument doc, Date date, EthBlock.Block block) {
        doc.field(OTransaction.OPROPERTY_CONFIRMED, true);
        doc.field(OTransaction.OPROPERTY_BLOCK, block.getNumber().toString());
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

    private <T> T getFromDocs(List<ODocument> docs, Function<ODocument, T> f) {
        return isDocsNotEmpty(docs) ? f.apply(docs.get(0)) : null;
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