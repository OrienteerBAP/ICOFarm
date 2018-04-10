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
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class DbServiceImpl implements IDbService {

    private transient final ICOFarmDBClosure dbClosure = new ICOFarmDBClosure();

    @Override
    public ICOFarmUser getUserBy(String field, String value) {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where "
                + field + " = ?", 1), value);
        return getFromDocs(docs, ICOFarmUser::new);
    }

    @Override
    public List<ICOFarmUser> getUsers() {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME));
        return docs == null || docs.isEmpty() ? Collections.emptyList() : docs.stream().map(ICOFarmUser::new)
                .collect(Collectors.toList());
    }

    @Override
    public OFunction getFunctionByName(String name) {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + OFunction.CLASS_NAME
                + " where name = ?", 1), name);
        return getFromDocs(docs, OFunction::new);
    }

    @Override
    public OMail getMailByName(String name) {
        List<ODocument> docs = query(new OSQLSynchQuery<>(String.format("select from %s where %s = ?",
                OMail.CLASS_NAME, OMail.NAME), 1), name);
        return getFromDocs(docs, OMail::new);
    }

    @Override
    public ORole getRoleByName(String name) {
        return DBClosure.sudo(db -> db.getMetadata().getSecurity().getRole(name));
    }

    @Override
    public List<Wallet> getWallets() {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME));
        return docs == null || docs.isEmpty() ? Collections.emptyList() : docs.stream().map(Wallet::new)
                .collect(Collectors.toList());
    }

    @Override
    public void confirmTransaction(Transaction transaction, EthBlock.Block block) {
        DBClosure.sudoConsumer(db -> {
            String sql = String.format("select from %s where %s = ?", OTransaction.CLASS_NAME, OTransaction.OPROPERTY_HASH);
            List<ODocument> docs = db.query(new OSQLSynchQuery<>(sql), transaction.getHash());
            Date date = new Date(1000 * block.getTimestamp().longValue());
            if (docs != null && !docs.isEmpty()) {
                docs.forEach(d -> {
                    d.field(OTransaction.OPROPERTY_CONFIRMED, true);
                    d.field(OTransaction.OPROPERTY_BLOCK, block.getNumber().toString());
                    d.field(OTransaction.OPROPERTY_TIMESTAMP, date);
                    d.save();
                });
            } else {
                saveUnconfirmedTransaction(transaction)
                        .setConfirmed(true)
                        .setBlock(block.getHash())
                        .setTimestamp(date)
                        .sudoSave();
            }
        });
    }

    @Override
    public boolean isICOFarmTransaction(Transaction transaction) {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME + " where "
                + Wallet.OPROPERTY_ADDRESS + " = ? OR "
                + Wallet.OPROPERTY_ADDRESS + " = ?", 1), transaction.getFrom(), transaction.getTo());
        return docs != null && !docs.isEmpty();
    }

    @Override
    public OTransaction saveUnconfirmedTransaction(Transaction transaction) {
        ODocument from = getUserByWalletAddress(transaction.getFrom());
        ODocument to = getUserByWalletAddress(transaction.getTo());
        OTransaction result = null;
        if (from != null) {
            result = new OTransaction(transaction, from)
                    .setConfirmed(false)
                    .sudoSave();
        }
        if (to != null && !to.equals(from)) {
            result = new OTransaction(transaction, to)
                    .setConfirmed(false)
                    .sudoSave();
        }
        return result;
    }

    @Override
    public List<ODocument> query(OSQLSynchQuery<ODocument> query, Object... args) {
        return dbClosure.setQuery(query)
                .setArgs(args)
                .execute();
    }

    private ODocument getUserByWalletAddress(String address) {
        String sql = String.format("select %s from %s where %s = ?", Wallet.OPROPERTY_OWNER, Wallet.CLASS_NAME, Wallet.OPROPERTY_ADDRESS);
        List<ODocument> docs = query(new OSQLSynchQuery<>(sql, 1), address);
        return docs != null && !docs.isEmpty() ? (ODocument) docs.get(0).field(Wallet.OPROPERTY_OWNER) : null;
    }

    private <T> T getFromDocs(List<ODocument> docs, Function<ODocument, T> f) {
        return docs != null && !docs.isEmpty() ? f.apply(docs.get(0)) : null;
    }

    private static class ICOFarmDBClosure extends DBClosure<List<ODocument>> {

        private OSQLSynchQuery<ODocument> query;
        private Object[] args;

        public ICOFarmDBClosure() {
            super();
        }

        @Override
        protected List<ODocument> execute(ODatabaseDocument db) {
            List<ODocument> docs = db.query(query, args);
            query = null;
            args = null;
            return docs;
        }

        private ICOFarmDBClosure setQuery(OSQLSynchQuery<ODocument> query) {
            this.query = query;
            return this;
        }

        private ICOFarmDBClosure setArgs(Object... args) {
            this.args = args;
            return this;
        }
    }
}
