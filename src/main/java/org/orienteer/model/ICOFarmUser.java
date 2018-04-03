package org.orienteer.model;

import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Date;

public class ICOFarmUser extends OUser {
	private static final long serialVersionUID = 1L;

	public static final String FIRST_NAME         = "firstName";
    public static final String LAST_NAME          = "lastName";
    public static final String EMAIL              = "email";
    public static final String ID                 = "id";
    public static final String RESTORE_ID         = "restoreId";
    public static final String RESTORE_ID_CREATED = "restoreIdCreated";
    public static final String ETH_WALLET = "ethereumWallet";

    public ICOFarmUser(ODocument iSource) {
        super(iSource);
    }

    public ICOFarmUser setFirstName(String firstName) {
        document.field(FIRST_NAME, firstName);
        return this;
    }

    public String getFirstName() {
        return document.field(FIRST_NAME);
    }

    public ICOFarmUser setLastName(String lastName) {
        document.field(LAST_NAME, lastName);
        return this;
    }

    public String getLastName() {
        return document.field(LAST_NAME);
    }

    public ICOFarmUser setEmail(String email) {
        document.field(EMAIL, email);
        return this;
    }

    public String getEmail() {
        return document.field(EMAIL);
    }

    public ICOFarmUser setId(String id) {
        document.field(ID, id);
        return this;
    }

    public String getId() {
        return document.field(ID);
    }

    public ICOFarmUser setRestoreId(String restoreId) {
        document.field(RESTORE_ID, restoreId);
        return this;
    }

    public String getRestoreId() {
        return document.field(RESTORE_ID);
    }

    public ICOFarmUser setRestoreIdCreated(Date date) {
        document.field(RESTORE_ID_CREATED, date);
        return this;
    }

    public Date getRestoreIdCreated() {
        return document.field(RESTORE_ID_CREATED);
    }

    public EthereumWallet getMainETHWallet() {
    	ODocument wallet = document.field(ETH_WALLET);
        return wallet!=null?new EthereumWallet(wallet):null;
    }

    public void sudoSave() {
        DBClosure.sudoSave(this);
    }
}
