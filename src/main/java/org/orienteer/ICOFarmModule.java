package org.orienteer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String TRANSACTION = "Transaction";
	public static final String CURRENCY    = "Currency";
	public static final String REFERRAL    = "Referral";
	public static final String WALLET      = "Wallet";
	public static final String MAIL_CONFIG = "ICOFarmMailConfig";

	public static final String OPROPERTY_TRANSACTION_DATETIME      = "dateTime";
	public static final String OPROPERTY_TRANSACTION_FROM_CURRENCY = "fromCurrency";
	public static final String OPROPERTY_TRANSACTION_FROM_VALUE    = "fromValue";
	public static final String OPROPERTY_TRANSACTION_TO_CURRENCY   = "toCurrency";
	public static final String OPROPERTY_TRANSACTION_TO_VALUE      = "toValue";
	public static final String OPROPERTY_TRANSACTION_OWNER         = "owner";

	public static final String OPROPERTY_REFERRAL_CREATED = "created";
	public static final String OPROPERTY_REFERRAL_USER    = "user";
	public static final String OPROPERTY_REFERRAL_BY      = "by";

	public static final String OPROPERTY_WALLET_OWNER    = "owner";
	public static final String OPROPERTY_WALLET_CURRENCY = "currency";

	public static final String OPROPERTY_MAIL_CONFIG_EMAIL     = "email";
	public static final String OPROPERTY_MAIL_CONFIG_PASSWORD  = "password";
	public static final String OPROPERTY_MAIL_CONFIG_SMTP_HOST = "smtpHost";
	public static final String OPROPERTY_MAIL_CONFIG_SMTP_PORT = "smtpPort";
	public static final String OPROPERTY_MAIL_CONFIG_FROM      = "from";
	public static final String OPROPERTY_MAIL_CONFIG_TYPE      = "type";

	protected ICOFarmModule() {
		super("ICOFarm", 15);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(OUser.CLASS_NAME)
				.oProperty("firstName", OType.STRING, 40)
				.oProperty("lastName", OType.STRING, 50)
				.oProperty("email", OType.STRING, 60).notNull()
				.oProperty("id", OType.STRING, 70).notNull();

		helper.oClass(CURRENCY, "OEnum");

		helper.oClass(TRANSACTION)
				.oProperty(OPROPERTY_TRANSACTION_DATETIME, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_TRANSACTION_FROM_CURRENCY, OType.LINK, 10).notNull().linkedClass(CURRENCY)
				.oProperty(OPROPERTY_TRANSACTION_FROM_VALUE, OType.DOUBLE, 20).notNull()
				.oProperty(OPROPERTY_TRANSACTION_TO_CURRENCY, OType.LINK, 30).notNull().linkedClass(CURRENCY)
				.oProperty(OPROPERTY_TRANSACTION_TO_VALUE, OType.DOUBLE, 40).notNull()
				.oProperty(OPROPERTY_TRANSACTION_OWNER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(REFERRAL)
				.oProperty(OPROPERTY_REFERRAL_CREATED, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_REFERRAL_USER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME).oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(OPROPERTY_REFERRAL_BY, OType.LINK, 20).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(WALLET)
				.oProperty(OPROPERTY_WALLET_OWNER, OType.LINK, 0).linkedClass(OUser.CLASS_NAME)
				.oProperty(OPROPERTY_WALLET_CURRENCY, OType.LINK, 10).linkedClass(CURRENCY);

		helper.oClass(MAIL_CONFIG)
				.oProperty(OPROPERTY_MAIL_CONFIG_EMAIL, OType.STRING, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_MAIL_CONFIG_PASSWORD, OType.STRING, 10).notNull().assignVisualization("password")
				.oProperty(OPROPERTY_MAIL_CONFIG_SMTP_HOST, OType.STRING, 20).notNull()
				.oProperty(OPROPERTY_MAIL_CONFIG_SMTP_PORT, OType.INTEGER, 30).notNull()
				.oProperty(OPROPERTY_MAIL_CONFIG_FROM, OType.STRING, 40)
				.oProperty(OPROPERTY_MAIL_CONFIG_TYPE, OType.STRING, 50).notNull();
		return null;
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
}
