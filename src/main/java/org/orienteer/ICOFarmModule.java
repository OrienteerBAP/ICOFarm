package org.orienteer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String TRANSACTION = "Transaction";
	public static final String CURRENCY    = "Currency";
	public static final String REFERRAL    = "Referral";
	public static final String WALLET      = "Wallet";

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


	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT    = "timeout";

	protected ICOFarmModule() {
		super("ICOFarm", 43);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		OClass user = helper.oClass(ICOFarmUser.CLASS_NAME)
				.oProperty(ICOFarmUser.FIRST_NAME, OType.STRING, 0)
				.oProperty(ICOFarmUser.LAST_NAME, OType.STRING, 10)
				.oProperty(ICOFarmUser.EMAIL, OType.STRING, 20).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(ICOFarmUser.ID, OType.STRING).notNull()
                .oProperty(ICOFarmUser.RESTORE_ID, OType.STRING).switchDisplayable(false)
                .oProperty(ICOFarmUser.RESTORE_ID_CREATED, OType.DATETIME).switchDisplayable(false).getOClass();

		updateUserCustomAttributes(user);

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

		createRemoveRestoreIdFunction(helper);
		return null;
	}


	private void updateUserCustomAttributes(OClass user) {
		ICOFarmApplication.REMOVE_CRON_RULE.setValue(user.getProperty(ICOFarmUser.RESTORE_ID), "0 0/1 * * * ?");
		ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.setValue(user.getProperty(ICOFarmUser.RESTORE_ID_CREATED), "86400000");
		CustomAttribute.ORDER.setValue(user.getProperty("locale"), "40");
		CustomAttribute.HIDDEN.setValue(user.getProperty(ICOFarmUser.ID), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty(ICOFarmUser.RESTORE_ID), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty(ICOFarmUser.RESTORE_ID_CREATED), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("name"), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("online"), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("status"), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("perspective"), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("perspectiveItem"), "true");
		CustomAttribute.HIDDEN.setValue(user.getProperty("lastSessionId"), "true");
	}

    /**
     * Create function which will remove user restoreId by scheduler
     * @param helper {@link OSchemaHelper} Orienteer helper
     */
	private void createRemoveRestoreIdFunction(OSchemaHelper helper) {
		Consumer<ODocument> save = ODocument::save;
		BiConsumer<ODatabaseDocument, String> removeFunc = (db, name) -> {
			db.command(new OCommandSQL("DELETE FROM " + OFunction.CLASS_NAME + " WHERE name = ?")).execute(name);
		};
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
			save.accept(doc);
		} catch (Exception e) {
			removeFunc.accept(helper.getDatabase(), FUN_REMOVE_RESTORE_ID_BY_EMAIL);
			save.accept(doc);
		}
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}
}
