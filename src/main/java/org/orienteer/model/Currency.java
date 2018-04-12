package org.orienteer.model;

import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.type.ODocumentWrapper;

import java.util.Map;

public class Currency extends ODocumentWrapper {
	private static final long serialVersionUID = 1L;

	public static final String CLASS_NAME = "Currency";

	public Currency() {
        super(CLASS_NAME);
    }

    public Currency(ORID iRID) {
        super(iRID);
    }

    public Currency(String iClassName) {
        super(iClassName);
    }

    public Currency(ODocument iDocument) {
        super(iDocument);
    }

    public Map<String, String> getNames() {
        return document.field("name");
    }

    public Currency setNames(Map<String, String> names) {
        document.field("name", names);
        return this;
    }

}
