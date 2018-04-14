package org.orienteer.module;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.*;
import org.orienteer.service.IEthereumUpdateService;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String CLASS_NAME = "ICOFarmModule";
	public static final String NAME       = "ICOFarm";

	public static final String REFERRAL     = "Referral";
	public static final String REGISTRATION = "Registration";

	public static final String OPROPERTY_REFERRAL_CREATED = "created";
	public static final String OPROPERTY_REFERRAL_USER    = "user";
	public static final String OPROPERTY_REFERRAL_BY      = "by";

	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT    = "timeout";

	public static final int VERSION = 5;

	public static final String EVENT_RESTORE_PASSWORD_PREFIX = "removeUserRestoreId";

	@Inject
	private IEthereumUpdateService updateService;

	protected ICOFarmModule() {
		super(NAME, VERSION);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);

		
		helper.oClass(Currency.CLASS_NAME, "OEnum");
		helper.oClass(TokenCurrency.CLASS_NAME, Currency.CLASS_NAME);
		
		helper.oClass(CLASS_NAME, ICOFarmModule.OMODULE_CLASS)
				.oProperty(EthereumClientConfig.OPROPERTY_NAME, OType.STRING, 0).markAsDocumentName().notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_HOST, OType.STRING, 10).notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_PORT, OType.INTEGER, 20).notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_WORK_FOLDER, OType.STRING, 30).notNull().defaultValue("icofarm")
				.oProperty(EthereumClientConfig.OPROPERTY_TIMEOUT, OType.INTEGER, 40).notNull().defaultValue("15")
				.oProperty(EthereumClientConfig.OPROPERTY_MAIN_TOKEN_CURRENCY, OType.LINK, 50).linkedClass(TokenCurrency.CLASS_NAME).notNull().assignVisualization("listbox");


		helper.oClass(OTransaction.CLASS_NAME)
				.oProperty(OTransaction.OPROPERTY_TIMESTAMP, OType.DATETIME, 0).markAsDocumentName().updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_FROM, OType.STRING, 10).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_TO, OType.STRING, 20).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_VALUE, OType.STRING, 30).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_HASH, OType.STRING, 40).notNull()
				.oProperty(OTransaction.OPROPERTY_BLOCK, OType.STRING, 50).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_CONFIRMED, OType.BOOLEAN, 60).notNull().updateCustomAttribute(CustomAttribute.UI_READONLY, true).defaultValue("false")
				.oProperty(OTransaction.OPROPERTY_WALLET, OType.LINK, 70);


		helper.oClass(REFERRAL)
				.oProperty(OPROPERTY_REFERRAL_CREATED, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_REFERRAL_USER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME).oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(OPROPERTY_REFERRAL_BY, OType.LINK, 20).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(Wallet.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_OWNER, OType.LINK, 0).linkedClass(ICOFarmUser.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_CURRENCY, OType.LINK, 10).linkedClass(Currency.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_BALANCE, OType.STRING, 20).updateCustomAttribute(CustomAttribute.UI_READONLY, "true")
				.oProperty(Wallet.OPROPERTY_ADDRESS, OType.STRING, 30)
                .oProperty(Wallet.OPROPERTY_CREATED, OType.DATETIME).updateCustomAttribute(CustomAttribute.HIDDEN, "true")
				.oProperty(Wallet.OPROPERTY_TRANSACTIONS, OType.LINKSET, 40).assignVisualization("table")
					.assignTab(Wallet.OPROPERTY_TRANSACTIONS);

		helper.oClass(REGISTRATION);

		helper.oClass(EmbeddedWallet.CLASS_NAME, Wallet.CLASS_NAME)
                .oProperty(EmbeddedWallet.OPROPERTY_NAME, OType.STRING).updateCustomAttribute(CustomAttribute.UI_READONLY, "true");

		helper.oClass(ExternalWallet.CLASS_NAME, Wallet.CLASS_NAME);

		helper.setupRelationship(Wallet.CLASS_NAME, Wallet.OPROPERTY_TRANSACTIONS, OTransaction.CLASS_NAME, OTransaction.OPROPERTY_WALLET);

		createRemoveRestoreIdFunction(helper);
		return createModuleDocument(helper);
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

	private ODocument createModuleDocument(OSchemaHelper helper) {
        List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>("select from " + CLASS_NAME, 1));
        if (docs != null && !docs.isEmpty()) {
            return null;
        }

        ODocument doc = new ODocument(CLASS_NAME);
        doc.field(EthereumClientConfig.OPROPERTY_NAME, NAME);
        doc.field(EthereumClientConfig.OPROPERTY_HOST, "http://localhost");
        doc.field(EthereumClientConfig.OPROPERTY_PORT, 8545);
        doc.field(EthereumClientConfig.OPROPERTY_TIMEOUT, 15);
        doc.field(EthereumClientConfig.OPROPERTY_WORK_FOLDER, "icofarm");
        doc.save();

        return doc;
	}

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
		super.onInitialize(app, db, moduleDoc);
		updateService.init(moduleDoc);
	}

	@Override
	public void onConfigurationChange(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
		super.onConfigurationChange(app, db, moduleDoc);
        onDestroy(app, db);
        onInitialize(app, db, moduleDoc);
	}

	@Override
	public void onDestroy(OrienteerWebApplication app, ODatabaseDocument db) {
		updateService.destroy();
	}
}
