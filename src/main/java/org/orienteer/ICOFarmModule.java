package org.orienteer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

	public static final String FUN_REMOVE_RESTORE_ID_EVERY_DAY                = "removeRestoreIdEveryDay";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT    = "timeout";

	protected ICOFarmModule() {
		super("ICOFarm", 34);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		helper.oClass(ICOFarmUser.CLASS_NAME)
				.oProperty(ICOFarmUser.FIRST_NAME, OType.STRING, 40)
				.oProperty(ICOFarmUser.LAST_NAME, OType.STRING, 50)
				.oProperty(ICOFarmUser.EMAIL, OType.STRING, 60).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(ICOFarmUser.ID, OType.STRING, 70).notNull()
                .oProperty(ICOFarmUser.RESTORE_ID, OType.STRING).switchDisplayable(false)
				.updateCustomAttribute(ICOFarmApplication.REMOVE_CRON, "0 0 0/1 1/1 * ? *")
				.updateCustomAttribute(ICOFarmApplication.REMOVE_TIMEOUT, "3600000")
                .oProperty(ICOFarmUser.RESTORE_ID_CREATED, OType.DATETIME).switchDisplayable(false);

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

		createRemoveRestoreIdFunction(helper);
		createRemoveRestoreIdEveryDayScheduler(helper);
		return null;
	}

    /**
     * Create function which will remove user restoreId by scheduler
     * @param helper {@link OSchemaHelper} Orienteer helper
     */
	private void createRemoveRestoreIdFunction(OSchemaHelper helper) {
		String code = String.format("var res = db.command('UPDATE OUser SET %s = null, %s = null WHERE %s = ? AND %s <= (sysdate() - ?)', email, timeout);\n"
				+ "if (res > 0) db.command('DELETE FROM OSchedule WHERE name = ?', eventName);",
                ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID_CREATED, ICOFarmUser.EMAIL, ICOFarmUser.RESTORE_ID_CREATED
        );
		List<String> params = new LinkedList<>();
		params.add(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL);
		params.add(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME);
		params.add(FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT);
		ODocument doc = helper.oClass(OFunction.CLASS_NAME).oDocument()
				.field("name", FUN_REMOVE_RESTORE_ID_BY_EMAIL)
				.field("language", "javascript")
				.field("code", code)
				.field("parameters", params).getODocument();
		try {
			doc.save();
		} catch (Exception e) {}
	}

	/**
	 * Create function which will remove all restoreId function every midnight if scheduler which must execute
	 * function `removeRestoreIdByEmail` don't executed it. (For example: in time when scheduler must execute function
	 * Orienteer server reloads or was offline)
	 * @param helper {@link OSchemaHelper} Orienteer helper
	 */
    private void createRemoveRestoreIdEveryDayScheduler(OSchemaHelper helper) {
		String code = String.format("UPDATE OUser SET %s = null, %s = null WHERE NOT (%s IS null) AND %s <= (sysdate() - 86400000)",
				ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID_CREATED, ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID_CREATED);

		ODocument doc = helper.oClass(OFunction.CLASS_NAME).oDocument()
				.field("name", FUN_REMOVE_RESTORE_ID_EVERY_DAY)
				.field("language", "sql")
				.field("code", code)
				.getODocument();

		ODocument scheduler = helper.oClass(OScheduledEvent.CLASS_NAME).oDocument()
				.field(OScheduledEvent.PROP_NAME, "removeRestoreIdEveryMidnight")
				.field(OScheduledEvent.PROP_FUNC, doc)
				.field(OScheduledEvent.PROP_RULE, "0 0 0 1/1 * ? *")
				.field(OScheduledEvent.PROP_STARTTIME, new Date())
				.getODocument();
		try {
			doc.save();
			scheduler.save();
		} catch (Exception e) {}
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
}
