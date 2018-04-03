package org.orienteer.model;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import org.orienteer.ICOFarmModule;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.orienteer.ICOFarmModule.*;

public class Transaction extends ODocumentWrapper {
	private static final long serialVersionUID = 1L;

	public Transaction() {
        super(ICOFarmModule.TRANSACTION);
    }

    public Transaction(ORID iRID) {
        super(iRID);
    }

    public Transaction(String iClassName) {
        super(iClassName);
    }

    public Transaction(ODocument iDocument) {
        super(iDocument);
    }

    public Transaction setOwner(OSecurityUser user) {
        document.field(OPROPERTY_TRANSACTION_OWNER, user.getDocument());
        return this;
    }

    public OUser getOwner() {
        return new OUser((ODocument) document.field(OPROPERTY_TRANSACTION_OWNER));
    }

    public Transaction setFromCurrency(Currency currency) {
        document.field(OPROPERTY_TRANSACTION_FROM_CURRENCY, currency);
        return this;
    }

    public Currency getFromCurrency() {
        return new Currency((ODocument) document.field(OPROPERTY_TRANSACTION_FROM_CURRENCY));
    }

    public Transaction setFromValue(double value) {
        document.field(OPROPERTY_TRANSACTION_FROM_VALUE, value);
        return this;
    }

    public double getFromValue() {
        return document.field(OPROPERTY_TRANSACTION_FROM_VALUE);
    }

    public Transaction setToCurrency(Currency currency) {
        document.field(OPROPERTY_TRANSACTION_TO_CURRENCY, currency);
        return this;
    }

    public Currency getToCurrency() {
        return new Currency((ODocument) document.field(OPROPERTY_TRANSACTION_TO_CURRENCY));
    }

    public Transaction setToValue(double value) {
        document.field(OPROPERTY_TRANSACTION_TO_VALUE, value);
        return this;
    }

    public double getToValue() {
        return document.field(OPROPERTY_TRANSACTION_TO_VALUE);
    }

    public Transaction setDateTime(Date dateTime) {
        document.field(OPROPERTY_TRANSACTION_DATETIME, dateTime);
        return this;
    }

    public Date getDateTime() {
        return document.field(OPROPERTY_TRANSACTION_DATETIME);
    }

    public Transaction sudoSave() {
        DBClosure.sudoSave(this);
        return this;
    }

    public static List<String> getUserFields() {
        List<String> list = new LinkedList<>();
        list.add(OPROPERTY_TRANSACTION_DATETIME);
        list.add(OPROPERTY_TRANSACTION_FROM_CURRENCY);
        list.add(OPROPERTY_TRANSACTION_FROM_VALUE);
        list.add(OPROPERTY_TRANSACTION_TO_CURRENCY);
        list.add(OPROPERTY_TRANSACTION_TO_VALUE);
        return Collections.unmodifiableList(list);
    }
}
