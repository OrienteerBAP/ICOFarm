package org.orienteer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduledEventBuilder;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";

	protected ICOFarmModule() {
		super("ICOFarm", 16);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		CustomAttribute attr = CustomAttribute.getOrCreate("remove.cron", OType.STRING, "", false, false);
		helper.oClass(ICOFarmUser.CLASS_NAME)
				.oProperty(ICOFarmUser.FIRST_NAME, OType.STRING, 40)
				.oProperty(ICOFarmUser.LAST_NAME, OType.STRING, 50)
				.oProperty(ICOFarmUser.EMAIL, OType.STRING, 60).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(ICOFarmUser.ID, OType.STRING, 70).notNull()
                .oProperty(ICOFarmUser.RESTORE_ID, OType.STRING).switchDisplayable(false).updateCustomAttribute(attr, "0 0 0/1 1/1 * ? *")
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

		createRemoveRestoreIdFunction(db);
		OFunction f = createRemoveRestoreIdEveryDayFunction(db);
		createRemoveRestoreIdScheduler(db, f);
		return null;
	}

    /**
     * Create function which will remove user restoreId by scheduler
     * @param db {@link ODatabaseDocument} Orienteer database
     */
	private void createRemoveRestoreIdFunction(ODatabaseDocument db) {
		OFunction f = db.getMetadata().getFunctionLibrary().createFunction("removeRestoreIdByEmail");
		f.setLanguage("javascript");
		f.setCode(String.format("db.command('UPDATE OUser SET %s = null, %s = null WHERE %s = ?', email);\n"
				+ "db.command('DELETE FROM oschedule WHERE name = ?', eventName);", ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID_CREATED, ICOFarmUser.EMAIL));
		f.setParameters(Arrays.asList(
				FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL,
				FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME
		));
		f.save();
	}

    /**
     * Create function which will remove all restoreId function every midnight if scheduler which must execute
     * function `removeRestoreIdByEmail` don't executed it. (For example: in time when scheduler must execute function
     * Orienteer server reloads or was offline)
     * @param db {@link ODatabaseDocument} Orienteer database
	 * @return {@link OFunction} this function
     */
	private OFunction createRemoveRestoreIdEveryDayFunction(ODatabaseDocument db) {
		String hourMillis = Long.toString(TimeUnit.MILLISECONDS.toHours(1));
	    OFunction f = db.getMetadata().getFunctionLibrary().createFunction(FUN_REMOVE_RESTORE_ID_BY_EMAIL);
	    f.setLanguage("sql");
	    f.setCode(String.format("UPDATE OUser SET %s = null WHERE NOT (%s IS null) AND %s < (sysdate() - %s)",
				ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID, ICOFarmUser.RESTORE_ID_CREATED, hourMillis));
	    f.save();
	    return f;
    }

    private void createRemoveRestoreIdScheduler(ODatabaseDocument db, OFunction f) {
		OScheduledEvent event = new OScheduledEventBuilder().setName("removeRestoreIdEveryMidnight")
				.setFunction(f)
				.setRule("0 0 0 1/1 * ? *")
				.build();
		db.getMetadata().getScheduler().scheduleEvent(event);
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
}
