package org.orienteer.service;

import com.google.inject.ImplementedBy;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.util.io.IClusterable;
import org.orienteer.model.EmbeddedWallet;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;

import java.util.List;

@ImplementedBy(DbServiceImpl.class)
public interface IDbService extends IClusterable {

    public ICOFarmUser getUserBy(String field, String value);

    public List<ICOFarmUser> getUsers();

    public OFunction getFunctionByName(String name);

    public OMail getMailByName(String name);

    public ORole getRoleByName(String name);

    public List<EmbeddedWallet> getEmbeddedWallets();

    public List<ODocument> query(OSQLSynchQuery<ODocument> query, Object...args);

}
