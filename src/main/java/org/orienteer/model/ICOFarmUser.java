package org.orienteer.model;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.util.*;

import static org.orienteer.module.ICOFarmSecurityModule.ORESTRICTED_ALLOW_READ;
import static org.orienteer.module.ICOFarmSecurityModule.ORESTRICTED_ALLOW_UPDATE;

public class ICOFarmUser extends OUser {
	private static final long serialVersionUID = 1L;

	public static final String OPROPERTY_FIRST_NAME         = "firstName";
    public static final String OPROPERTY_LAST_NAME          = "lastName";
    public static final String OPROPERTY_EMAIL              = "email";
    public static final String OPROPERTY_ID                 = "id";
    public static final String OPROPERTY_RESTORE_ID         = "restoreId";
    public static final String ORPOPERTY_RESTORE_ID_CREATED = "restoreIdCreated";
    public static final String OPROPERTY_ETH_WALLET         = "ethereumWallet";

    public ICOFarmUser() {
        super(ICOFarmUser.CLASS_NAME);
    }

    public ICOFarmUser(ODocument iSource) {
        super(iSource);
    }

    public ICOFarmUser setFirstName(String firstName) {
        document.field(OPROPERTY_FIRST_NAME, firstName);
        return this;
    }

    public String getFirstName() {
        return document.field(OPROPERTY_FIRST_NAME);
    }

    public ICOFarmUser setLastName(String lastName) {
        document.field(OPROPERTY_LAST_NAME, lastName);
        return this;
    }

    public String getLastName() {
        return document.field(OPROPERTY_LAST_NAME);
    }

    public ICOFarmUser setEmail(String email) {
        document.field(OPROPERTY_EMAIL, email);
        return this;
    }

    public String getEmail() {
        return document.field(OPROPERTY_EMAIL);
    }

    public ICOFarmUser setId(String id) {
        document.field(OPROPERTY_ID, id);
        return this;
    }

    public String getId() {
        return document.field(OPROPERTY_ID);
    }

    public ICOFarmUser setRestoreId(String restoreId) {
        document.field(OPROPERTY_RESTORE_ID, restoreId);
        return this;
    }

    public String getRestoreId() {
        return document.field(OPROPERTY_RESTORE_ID);
    }

    public ICOFarmUser setRestoreIdCreated(Date date) {
        document.field(ORPOPERTY_RESTORE_ID_CREATED, date);
        return this;
    }

    public ICOFarmUser setActive(boolean active) {
        setAccountStatus(active ? STATUSES.ACTIVE : STATUSES.SUSPENDED);
        Set<OIdentifiable> readers = document.field(ORESTRICTED_ALLOW_READ);
        Set<OIdentifiable> updaters = document.field(ORESTRICTED_ALLOW_UPDATE);
        if (readers == null) readers = new HashSet<>();
        if (updaters == null) updaters = new HashSet<>();

        readers.add(document);
        updaters.add(document);

        document.field(ORESTRICTED_ALLOW_READ, readers);
        document.field(ORESTRICTED_ALLOW_UPDATE, updaters);

        return this;
    }

    public boolean isActive() {
        return getAccountStatus() == STATUSES.ACTIVE;
    }

    public Date getRestoreIdCreated() {
        return document.field(ORPOPERTY_RESTORE_ID_CREATED);
    }

    public Wallet getMainETHWallet() {
    	ODocument wallet = document.field(OPROPERTY_ETH_WALLET);
        return wallet != null ? new Wallet(wallet) : null;
    }
}
