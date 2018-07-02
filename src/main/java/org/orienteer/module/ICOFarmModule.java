package org.orienteer.module;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.schedule.OScheduledEvent;
import com.orientechnologies.orient.core.schedule.OScheduler;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.visualizer.HashVisualizer;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.tasks.OTask;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.*;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumUpdateService;
import org.orienteer.tasks.LoadTokenTransactionsTask;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String CLASS_NAME = "ICOFarmModule";
	public static final String NAME       = "ICOFarm";

	public static final String REFERRAL     = "Referral";
	public static final String REGISTRATION = "Registration";

	public static final String BUY_TOKENS      = "BuyTokens";
	public static final String TRANSFER_TOKENS = "TransferTokens";
	public static final String MONEY           = "Money";

	public static final String OPROPERTY_REFERRAL_CREATED = "created";
	public static final String OPROPERTY_REFERRAL_USER    = "user";
	public static final String OPROPERTY_REFERRAL_BY      = "by";

	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL                 = "removeRestoreIdByEmail";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EMAIL      = "email";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_EVENT_NAME = "eventName";
	public static final String FUN_REMOVE_RESTORE_ID_BY_EMAIL_ARGS_TIMEOUT    = "timeout";

	public static final String EVENT_RESTORE_PASSWORD_PREFIX = "removeUserRestoreId";

	public static final String ETH  = "ETH";
	public static final String GWEI = "GWEI";
	public static final String WEI  = "WEI";

	public static final String ZERO_ADDRESS = "0x0";

	public static final String REGISTRATION_MAIL_NAME = "registration";
	public static final String RESTORE_MAIL_NAME      = "restore";

	public static final int VERSION = 1;

	@Inject
	private IEthereumUpdateService updateService;

	@Inject
	private IDBService dbService;

	protected ICOFarmModule() {
		super(NAME, VERSION);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		OSchemaHelper helper = OSchemaHelper.bind(db);

		helper.oClass(Wallet.CLASS_NAME);

		helper.oClass(Token.CLASS_NAME)
				.oProperty(Token.OPROPERTY_NAME, OType.EMBEDDEDMAP, 0).assignVisualization("localization").markAsDocumentName().updateCustomAttribute(CustomAttribute.DISPLAYABLE, true)
				.oProperty(Token.OPROPERTY_DESCRIPTION, OType.STRING, 10)
				.oProperty(Token.OPROPERTY_SYMBOL, OType.STRING, 20).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE).updateCustomAttribute(CustomAttribute.DISPLAYABLE, true)
				.oProperty(Token.OPROPERTY_ADDRESS, OType.STRING, 30).notNull().assignVisualization(HashVisualizer.NAME).updateCustomAttribute(CustomAttribute.DISPLAYABLE, true)
				.oProperty(Token.OPROPERTY_ETHER_COST, OType.DECIMAL, 40).notNull().defaultValue("0").min("0").updateCustomAttribute(CustomAttribute.DISPLAYABLE, true)
				.oProperty(Token.OPROPERTY_OWNER, OType.LINK, 50).linkedClass(Wallet.CLASS_NAME).updateCustomAttribute(CustomAttribute.DISPLAYABLE, false);
		
		helper.oClass(CLASS_NAME, ICOFarmModule.OMODULE_CLASS)
				.oProperty(EthereumClientConfig.OPROPERTY_NAME, OType.STRING, 0).markAsDocumentName().notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_HOST, OType.STRING, 10).notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_PORT, OType.INTEGER, 20).notNull()
				.oProperty(EthereumClientConfig.OPROPERTY_TIMEOUT, OType.INTEGER, 40).notNull().defaultValue("15")
				.oProperty(EthereumClientConfig.OPROPERTY_TRANSACTIONS_BUFFER_DELAY, OType.INTEGER, 50).notNull().defaultValue("5")
				.oProperty(EthereumClientConfig.OPROPERTY_TRANSACTIONS_BUFFER_NUM, OType.INTEGER, 60).notNull().defaultValue("100")
				.oProperty(EthereumClientConfig.OPROPERTY_MAIN_TOKEN, OType.LINK, 70).linkedClass(Token.CLASS_NAME).updateCustomAttribute(CustomAttribute.DISPLAYABLE, true);


		helper.oClass(OTransaction.CLASS_NAME)
				.oProperty(OTransaction.OPROPERTY_HASH, OType.STRING, 0).notNull().assignVisualization(HashVisualizer.NAME).oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(OTransaction.OPROPERTY_FROM, OType.STRING, 10).updateCustomAttribute(CustomAttribute.UI_READONLY, true).assignVisualization(HashVisualizer.NAME)
				.oProperty(OTransaction.OPROPERTY_TO, OType.STRING, 20).updateCustomAttribute(CustomAttribute.UI_READONLY, true).assignVisualization(HashVisualizer.NAME)
				.oProperty(OTransaction.OPROPERTY_VALUE, OType.STRING, 30).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_TOKENS, OType.DECIMAL, 40).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_CURRENCY, OType.LINK, 50).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_BLOCK, OType.STRING, 60).updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_TIMESTAMP, OType.DATETIME, 70).markAsDocumentName().updateCustomAttribute(CustomAttribute.UI_READONLY, true)
				.oProperty(OTransaction.OPROPERTY_CONFIRMED, OType.BOOLEAN, 80).notNull()
				.updateCustomAttribute(CustomAttribute.UI_READONLY, true).defaultValue("false");


		helper.oClass(REFERRAL)
				.oProperty(OPROPERTY_REFERRAL_CREATED, OType.DATETIME, 0).notNull().markAsDocumentName()
				.oProperty(OPROPERTY_REFERRAL_USER, OType.LINK, 10).notNull().linkedClass(OUser.CLASS_NAME).oIndex(OClass.INDEX_TYPE.UNIQUE)
				.oProperty(OPROPERTY_REFERRAL_BY, OType.LINK, 20).notNull().linkedClass(OUser.CLASS_NAME);

		helper.oClass(Wallet.CLASS_NAME)
				.oProperty(Wallet.OPROPERTY_NAME, OType.STRING, 0).markAsDocumentName()
				.oProperty(Wallet.OPROPERTY_OWNER, OType.LINK, 10)
				.oProperty(Wallet.OPROPERTY_ADDRESS, OType.STRING, 30).assignVisualization(HashVisualizer.NAME)
				.oProperty(Wallet.OPROPERTY_WALLET_JSON, OType.BINARY, 40).updateCustomAttribute(CustomAttribute.DISPLAYABLE, false)
				.oProperty(Wallet.OPROPERTY_BALANCES, OType.EMBEDDEDMAP, 50).linkedType(OType.DECIMAL).updateCustomAttribute(CustomAttribute.UI_READONLY, true).notNull()
				.updateCustomAttribute(CustomAttribute.DISPLAYABLE, true)
				.oProperty(Wallet.OPROPERTY_DISPLAYABLE_TOKEN, OType.LINK, 60).linkedClass(Token.CLASS_NAME).assignVisualization("listbox").notNull()
				.oProperty(Wallet.OPROPERTY_CREATED, OType.DATETIME).updateCustomAttribute(CustomAttribute.HIDDEN, "true");

		helper.oClass(ICOFarmUser.CLASS_NAME)
				.oProperty(ICOFarmUser.OPROPERTY_WALLETS, OType.LINKSET);//.updateCustomAttribute(CustomAttribute.UI_READONLY, true);

		helper.oClass(LoadTokenTransactionsTask.CLASS_NAME, OTask.TASK_CLASS)
				.oProperty(LoadTokenTransactionsTask.OPROPERTY_TOKEN, OType.LINK).linkedClass(Token.CLASS_NAME).notNull()
				.oProperty(LoadTokenTransactionsTask.OPROPERTY_START_BLOCK, OType.STRING).notNull()
				.oProperty(LoadTokenTransactionsTask.OPROPERTY_END_BLOCK, OType.STRING).notNull();

		helper.setupRelationship(ICOFarmUser.CLASS_NAME, ICOFarmUser.OPROPERTY_WALLETS, Wallet.CLASS_NAME, Wallet.OPROPERTY_OWNER);

		helper.oClass(REGISTRATION);
		helper.oClass(BUY_TOKENS);
		helper.oClass(TRANSFER_TOKENS);
		helper.oClass(MONEY);

		createRemoveRestoreIdFunction(helper);
		createDefaultTokens();
		createDefaultMails(helper);
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
                ICOFarmUser.OPROPERTY_RESTORE_ID, ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED, ICOFarmUser.OPROPERTY_EMAIL, ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED
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
        doc.save();

        return doc;
	}

	private void createDefaultTokens() {

		if (dbService.getTokenBySymbol(ETH) == null) {
			createEthereumCurrency().setNames(CommonUtils.toMap(Locale.ENGLISH.toLanguageTag(), "Ether"))
					.setSymbol(ETH)
					.setEtherCost(BigDecimal.ONE).save();
		}

		if (dbService.getTokenBySymbol(GWEI) == null) {
			createEthereumCurrency().setNames(CommonUtils.toMap(Locale.ENGLISH.toLanguageTag(), "Gwei"))
					.setSymbol(GWEI)
					.setEtherCost(new BigDecimal("0.000000001")).save();
		}

		if (dbService.getTokenBySymbol(WEI) == null) {
			createEthereumCurrency().setNames(CommonUtils.toMap(Locale.ENGLISH.toLanguageTag(), "Wei"))
					.setSymbol(WEI)
					.setEtherCost(new BigDecimal("0.000000000000000001")).save();
		}
	}

	private Token createEthereumCurrency() {
		return new Token().setAddress(ZERO_ADDRESS);
	}

	private void createDefaultMails(OSchemaHelper helper) {
	    ODatabaseDocument db = helper.getDatabase();
	    List<ODocument> docs = db.query(new OSQLSynchQuery<>("select from " + OMailSettings.CLASS_NAME), 1);
	    ODocument config = docs != null && !docs.isEmpty() ? docs.get(0) : null;
	    if (config == null) {
            config = createDefaultMailConfig();
        }

        if (!isMailExists(RESTORE_MAIL_NAME, db)) {
	        createMail(
	                RESTORE_MAIL_NAME,
                    new ResourceModel("mail.default.restore.subject").getObject(),
                    new ResourceModel("mail.default.restore.from").getObject(),
                    new ResourceModel("mail.default.restore.text").getObject(),
                    config
            );
        }

        if (!isMailExists(REGISTRATION_MAIL_NAME, db)) {
            createMail(
                    REGISTRATION_MAIL_NAME,
                    new ResourceModel("mail.default.registration.subject").getObject(),
                    new ResourceModel("mail.default.registration.from").getObject(),
                    new ResourceModel("mail.default.registration.text").getObject(),
                    config
            );
        }
    }

    private ODocument createDefaultMailConfig() {
	    ODocument doc = new ODocument(OMailSettings.CLASS_NAME);
	    doc.field(OMailSettings.EMAIL, "icofarm.default@gmail.com");
	    doc.field(OMailSettings.PASSWORD, "default");
	    doc.field(OMailSettings.SMTP_HOST, "smtp.gmail.com");
	    doc.field(OMailSettings.SMTP_PORT, 587);
	    doc.field(OMailSettings.TLS_SSL, true);
	    doc.save();

	    return doc;
    }

    private void createMail(String name, String subject, String from, String text, ODocument settings) {
	    ODocument doc = new ODocument(OMail.CLASS_NAME);
	    doc.field(OMail.NAME, name);
        doc.field(OMail.SUBJECT, subject);
	    doc.field(OMail.FROM, from);
	    doc.field(OMail.TEXT, text);
        doc.field(OMail.SETTINGS, settings);
	    doc.save();
    }


    private boolean isMailExists(String name, ODatabaseDocument db) {
        List<ODocument> docs = db.query(new OSQLSynchQuery<>("select from " + OMail.CLASS_NAME + " where name = ?", 1), name);
        return docs != null && !docs.isEmpty();
    }

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
		super.onInitialize(app, db, moduleDoc);
		updateService.init(moduleDoc);
		OScheduler scheduler = db.getMetadata().getScheduler();
		Collection<OScheduledEvent> events = scheduler.getEvents().values(); // TODO: remove after fix issue https://github.com/orientechnologies/orientdb/issues/8368
		for (OScheduledEvent event : events) {
			scheduler.updateEvent(event);
		}
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
