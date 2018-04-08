package org.orienteer.model;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class OTransaction extends ODocumentWrapper {

    public static final String CLASS_NAME = "OTransaction";

    public static final String OPROPERTY_DATETIME      = "dateTime";
    public static final String OPROPERTY_FROM_CURRENCY = "fromCurrency";
    public static final String OPROPERTY_FROM_VALUE    = "fromValue";
    public static final String OPROPERTY_TO_CURRENCY   = "toCurrency";
    public static final String OPROPERTY_TO_VALUE      = "toValue";
    public static final String OPROPERTY_OWNER         = "owner";

	private static final long serialVersionUID = 1L;

	public OTransaction() {
        super(CLASS_NAME);
    }

    public OTransaction(ORID iRID) {
        super(iRID);
    }

    public OTransaction(String iClassName) {
        super(iClassName);
    }

    public OTransaction(ODocument iDocument) {
        super(iDocument);
    }

    public OTransaction setOwner(OSecurityUser user) {
        document.field(OPROPERTY_OWNER, user.getDocument());
        return this;
    }

    public OUser getOwner() {
        return new OUser((ODocument) document.field(OPROPERTY_OWNER));
    }

    public OTransaction setFromCurrency(Currency currency) {
        document.field(OPROPERTY_FROM_CURRENCY, currency);
        return this;
    }

    public Currency getFromCurrency() {
        return new Currency((ODocument) document.field(OPROPERTY_FROM_CURRENCY));
    }

    public OTransaction setFromValue(double value) {
        document.field(OPROPERTY_FROM_VALUE, value);
        return this;
    }

    public double getFromValue() {
        return document.field(OPROPERTY_FROM_VALUE);
    }

    public OTransaction setToCurrency(Currency currency) {
        document.field(OPROPERTY_TO_CURRENCY, currency);
        return this;
    }

    public Currency getToCurrency() {
        return new Currency((ODocument) document.field(OPROPERTY_TO_CURRENCY));
    }

    public OTransaction setToValue(double value) {
        document.field(OPROPERTY_TO_VALUE, value);
        return this;
    }

    public double getToValue() {
        return document.field(OPROPERTY_TO_VALUE);
    }

    public OTransaction setDateTime(Date dateTime) {
        document.field(OPROPERTY_DATETIME, dateTime);
        return this;
    }

    public Date getDateTime() {
        return document.field(OPROPERTY_DATETIME);
    }

    public OTransaction sudoSave() {
        DBClosure.sudoSave(this);
        return this;
    }

    public static List<String> getUserFields() {
        List<String> list = new LinkedList<>();
        list.add(OPROPERTY_DATETIME);
        list.add(OPROPERTY_FROM_CURRENCY);
        list.add(OPROPERTY_FROM_VALUE);
        list.add(OPROPERTY_TO_CURRENCY);
        list.add(OPROPERTY_TO_VALUE);
        return Collections.unmodifiableList(list);
    }
}
