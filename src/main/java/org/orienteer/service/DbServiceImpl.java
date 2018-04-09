package org.orienteer.service;

import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.model.*;
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
    public EthereumClientConfig getEthereumClientConfig() {
        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + EthereumClientConfig.CLASS_NAME, 1));
        return getFromDocs(docs, EthereumClientConfig::new);
    }

    @Override
    public boolean isICOFarmTransaction(Transaction transaction) {

        List<ODocument> docs = query(new OSQLSynchQuery<>("select from " + Wallet.CLASS_NAME + " where "
                + Wallet.OPROPERTY_ADDRESS + " = ? OR "
                + Wallet.OPROPERTY_ADDRESS + " = ?", 1), transaction.getFrom(), transaction.getTo());
        return docs != null && !docs.isEmpty();
    }

    @Override
    public void saveTransaction(Transaction transaction, Date timestamp) {
        new OTransaction(transaction, timestamp).sudoSave();
    }

    @Override
    public List<ODocument> query(OSQLSynchQuery<ODocument> query, Object... args) {
        return dbClosure.setQuery(query)
                .setArgs(args)
                .execute();
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
