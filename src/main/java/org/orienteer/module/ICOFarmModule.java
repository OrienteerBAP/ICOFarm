package org.orienteer.module;

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
import org.orienteer.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String REFERRAL     = "Referral";
	public static final String REGISTRATION = "Registration";

	public static final String OPROPERTY_REFERRAL_CREATED = "created";
	public static final String OPROPERTY_REFERRAL_USER    = "user";
	public static final String OPROPERTY_REFERRAL_BY      = "by";

	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT    = "timeout";

	public static final String WALLETS_DIR = "/tmp/icofarm/";

	public static final int VERSION = 0;


	protected ICOFarmModule() {
		super("ICOFarm", VERSION);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);

		helper.oClass(Currency.CLASS_NAME, "OEnum");

		helper.oClass(OTransaction.CLASS_NAME)
				.oProperty(OTransaction.OPROPERTY_DATETIME, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OTransaction.OPROPERTY_FROM_CURRENCY, OType.LINK, 10).notNull().linkedClass(Currency.CLASS_NAME)
				.oProperty(OTransaction.OPROPERTY_FROM_VALUE, OType.DOUBLE, 20).notNull()
				.oProperty(OTransaction.OPROPERTY_TO_CURRENCY, OType.LINK, 30).notNull().linkedClass(Currency.CLASS_NAME)
				.oProperty(OTransaction.OPROPERTY_TO_VALUE, OType.DOUBLE, 40).notNull()
				.oProperty(OTransaction.OPROPERTY_OWNER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(REFERRAL)
				.oProperty(OPROPERTY_REFERRAL_CREATED, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_REFERRAL_USER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME).oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(OPROPERTY_REFERRAL_BY, OType.LINK, 20).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(Wallet.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_OWNER, OType.LINK, 0).linkedClass(ICOFarmUser.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_CURRENCY, OType.LINK, 10).linkedClass(Currency.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_BALANCE, OType.STRING, 20).updateCustomAttribute(CustomAttribute.UI_READONLY, "true");

		helper.oClass(REGISTRATION);

		helper.oClass(EmbeddedWallet.CLASS_NAME, Wallet.CLASS_NAME)
                .oProperty(EmbeddedWallet.OPROPERTY_NAME, OType.STRING).updateCustomAttribute(CustomAttribute.UI_READONLY, "true")
                .oProperty(EmbeddedWallet.OPROPERTY_PASSWORD, OType.STRING).updateCustomAttribute(CustomAttribute.UI_READONLY, "true")
				.oProperty(EmbeddedWallet.OPROPERTY_ADDRESS, OType.STRING).updateCustomAttribute(CustomAttribute.UI_READONLY, "true");

		createRemoveRestoreIdFunction(helper);

		return null;
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
