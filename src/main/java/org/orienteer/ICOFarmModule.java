package org.orienteer;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OSchema;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.metadata.security.*;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.CustomAttribute;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.OWidgetsModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.util.CommonUtils;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;

public class ICOFarmModule extends AbstractOrienteerModule {

	public static final String TRANSACTION           = "Transaction";
	public static final String CURRENCY              = "Currency";
	public static final String REFERRAL              = "Referral";
	public static final String WALLET                = "Wallet";
	public static final String EXTERNAL_WALLET       = "ExternalWallet";
	public static final String EMBEDDED_WALLET       = "EmbeddedWallet";
	public static final String CRYPTOCURRENCY_WALLET = "CryptocurrencyWallet";
	public static final String ETHEREUM_WALLET       = "EthereumWallet";
	public static final String REGISTRATION = "Registration";

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

	public static final String INVESTOR_ROLE         = "investor";
	public static final String INVESTOR_PERSPECTIVE  = "Investor";
	public static final String ANONYMOUS_PERSPECTIVE = "Anonymous";

	public static final String REFERRAL_WIDGET_ID       = "referrals-widget";
	public static final String REGISTRATION_WIDGET_ID   = "registration";
	public static final String SCHEMA_CLASSES_WIDGET_ID = "list-oclasses";

	public static final String ORESTRICTED_ALLOW        = "_allow";
	public static final String ORESTRICTED_ALLOW_READ   = "_allowRead";
	public static final String ORESTRICTED_ALLOW_UPDATE = "_allowUpdate";
	public static final String ORESTRICTED_ALLOW_DELETE = "_allowDelete";

	/**
	 * Contains hidden properties for investors.
	 * key - class name
	 * value - list with hidden properties
	 */
	public static final Map<String, List<String>> HIDDEN_PROPERTIES = new HashMap<>();

	/**
	 * Contains hidden widgets for investors
	 * key - class name
	 * value - hidden widget
	 */
	public static final Map<String, List<String>> HIDDEN_WIDGETS = new HashMap<>();

	protected ICOFarmModule() {
		super("ICOFarm", 1);
	}
	
	@Override
	public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
		super.onInstall(app, db);
		OSchemaHelper helper = OSchemaHelper.bind(db);
		adjustOUserClass(helper);

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

		helper.oClass(REGISTRATION);

		updateClassRestrictions(db);
		createRemoveRestoreIdFunction(helper);
		updateReaderPermissions(db);
		updateInvestorPermissions(db);
		createPerspectives(helper);
		updatePerspectivesPermissions(helper, db);
		adjustDashboard(helper);
		return null;
	}


	private void updateUserCustomAttributes(OClass user) {
		ICOFarmApplication.REMOVE_CRON_RULE.setValue(user.getProperty(ICOFarmUser.RESTORE_ID), "0 0/1 * * * ?");
		ICOFarmApplication.REMOVE_SCHEDULE_START_TIMEOUT.setValue(user.getProperty(ICOFarmUser.RESTORE_ID_CREATED), "86400000");
		CustomAttribute.ORDER.setValue(user.getProperty("locale"), "40");
	}

	private void updateInvestorPermissions(ODatabaseDocument db) {
		OSecurity security = db.getMetadata().getSecurity();

		ORole investor = security.getRole(INVESTOR_ROLE) != null ? security.getRole(INVESTOR_ROLE) :
				security.createRole(INVESTOR_ROLE, OSecurityRole.ALLOW_MODES.DENY_ALL_BUT).setParentRole(security.getRole("reader"));

		investor.grant(ResourceGeneric.CLASS, TRANSACTION, 7);

		investor.grant(ResourceGeneric.CLASS, WALLET, 7);
		investor.grant(ResourceGeneric.CLASS, EXTERNAL_WALLET, 7);
		investor.grant(ResourceGeneric.CLASS, EMBEDDED_WALLET, 7);
		investor.grant(ResourceGeneric.CLASS, CRYPTOCURRENCY_WALLET, 7);
		investor.grant(ResourceGeneric.CLASS, ETHEREUM_WALLET, 7);
		investor.grant(ResourceGeneric.CLASS, CURRENCY, 2);
		investor.grant(ResourceGeneric.CLASS, REFERRAL, 2);
		investor.grant(ResourceGeneric.CLASS, OUser.CLASS_NAME, 6);
		investor.grant(ResourceGeneric.CLASS, REGISTRATION, 0);

		investor.grant(ResourceGeneric.CLUSTER, "*", 7);

		investor.save();
	}

	private void updateReaderPermissions(ODatabaseDocument db) {
		ORole reader = db.getMetadata().getSecurity().getRole("reader");
		reader.grant(ResourceGeneric.CLASS, null, 0);
		reader.grant(ResourceGeneric.CLASS, ORole.CLASS_NAME, 0);
		reader.grant(ResourceGeneric.CLASS, REGISTRATION, 2);
		reader.grant(ResourceGeneric.CLASS, OWidgetsModule.OCLASS_WIDGET, 2);
		reader.grant(ResourceGeneric.CLASS, OWidgetsModule.OCLASS_DASHBOARD, 2);
		reader.grant(ResourceGeneric.CLASS, PerspectivesModule.OCLASS_ITEM, 2);
		reader.grant(ResourceGeneric.CLASS, PerspectivesModule.OCLASS_PERSPECTIVE, 2);

		reader.save();
	}

	private void createPerspectives(OSchemaHelper helper) {
		Function<String, ODocument> getOrCreatePerspective = (name) -> {
			PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
			ODocument doc = perspectivesModule.getPerspectiveByName(helper.getDatabase(), name);
			if (doc == null) {
				doc = new ODocument(PerspectivesModule.OCLASS_PERSPECTIVE);
				doc.field("name", CommonUtils.toMap("en", name));
			}
			return doc;
		};
		BiFunction<String, ODocument, ODocument> getOrCreatePerspectiveItem = (name, perspective) -> {
			List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>("select from " + PerspectivesModule.OCLASS_ITEM
					+ " where name.values() contains ? and perspective = ?"), name, perspective);
			ODocument doc = docs != null && !docs.isEmpty() ? docs.get(0) : null;
			if (doc == null) {
				doc = new ODocument(PerspectivesModule.OCLASS_ITEM);
				doc.field("name", CommonUtils.toMap("en", name));
			}
			return doc;
		};

		ODocument perspective = getOrCreatePerspective.apply(INVESTOR_PERSPECTIVE);
		perspective.field("icon", FAIconType.usd.name());
		perspective.field("homeUrl", "/browse/" + TRANSACTION);
		perspective.save();

		ODocument item1 = getOrCreatePerspectiveItem.apply("Transactions", perspective);
		item1.field("icon", FAIconType.usd.name());
		item1.field("perspective", perspective);
		item1.field("url", "/browse/" + TRANSACTION);
		item1.save();

		ODocument item2 = getOrCreatePerspectiveItem.apply("Referrals", perspective);
		item2.field("icon", FAIconType.users.name());
		item2.field("perspective", perspective);
		item2.field("url", "/browse/" + REFERRAL);
		item2.save();

		ODocument item3 = getOrCreatePerspectiveItem.apply("Wallets", perspective);
		item3.field("icon", FAIconType.briefcase.name());
		item3.field("perspective", perspective);
		item3.field("url", "/browse/" + WALLET);
		item3.save();

		perspective.field("menu", Arrays.asList(item1, item2, item3));
		perspective.save();

		perspective = getOrCreatePerspective.apply(ANONYMOUS_PERSPECTIVE);
		perspective.field("icon", FAIconType.user_secret.name());
		perspective.field("homeUrl", "/browse/Registration");
		perspective.save();

		item1 = getOrCreatePerspectiveItem.apply("Registration", perspective);
		item1.field("icon", FAIconType.info.name());
		item1.field("perspective", perspective);
		item1.field("url", "/browse/Registration");
		item1.save();

		perspective.field("menu", Collections.singletonList(item1));
		perspective.save();
	}


	private void updatePerspectivesPermissions(OSchemaHelper helper, ODatabaseDocument db) {
		OClass restricted = helper.oClass("ORestricted").getOClass();
		OClass perspectiveClass = helper.oClass(PerspectivesModule.OCLASS_PERSPECTIVE).getOClass();
		if (!perspectiveClass.isSubClassOf(restricted)) perspectiveClass.addSuperClass(restricted);

		OSecurity security = db.getMetadata().getSecurity();
		PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);

		Consumer<List<String>> updateRole = (list) -> {
			ODocument perspective = perspectivesModule.getPerspectiveByName(db, list.get(0));
			ORole role = security.getRole(list.get(1));
			role.getDocument().field("perspective", perspective);
			perspective.field(list.get(2), role.getDocument());
			role.save();
			perspective.save();
		};
		Consumer<List<String>> updateUser = (list) -> {
			ODocument perspective = perspectivesModule.getPerspectiveByName(db, list.get(0));
			OUser user = security.getUser(list.get(1));
			user.getDocument().field("perspective", perspective);
			perspective.field(list.get(2), user.getDocument());
			user.save();
			perspective.save();
		};

		updateUser.accept(Arrays.asList(ANONYMOUS_PERSPECTIVE, "reader", ORESTRICTED_ALLOW_READ));
		updateRole.accept(Arrays.asList(INVESTOR_PERSPECTIVE, INVESTOR_ROLE, ORESTRICTED_ALLOW_READ));
	}

	private void updateClassRestrictions(ODatabaseDocument db) {
		OSchema schema = db.getMetadata().getSchema();
		OClass restricted = schema.getClass("ORestricted");
		Consumer<OClass> setRestricted = (c) -> {
			if (!c.isSubClassOf(restricted)) {
				c.addSuperClass(restricted);
				Collection<OProperty> properties = restricted.properties();
				c.properties().stream().filter(p -> !properties.contains(p))
						.filter(p -> !(boolean) CustomAttribute.HIDDEN.getValue(p))
						.forEach(p -> CustomAttribute.DISPLAYABLE.setValue(p, true));
			}
		};

		setRestricted.accept(schema.getClass(TRANSACTION));
		setRestricted.accept(schema.getClass(REFERRAL));
		setRestricted.accept(schema.getClass(WALLET));
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

	private void adjustOUserClass(OSchemaHelper helper) {
		updateDefaultOrientDbUsers(helper.getDatabase());

		OClass user = helper.oClass(ICOFarmUser.CLASS_NAME)
				.oProperty(ICOFarmUser.FIRST_NAME, OType.STRING, 0)
				.oProperty(ICOFarmUser.LAST_NAME, OType.STRING, 10)
				.oProperty(ICOFarmUser.EMAIL, OType.STRING, 20).notNull().oIndex(OClass.INDEX_TYPE.UNIQUE).markAsDocumentName()
				.oProperty(ICOFarmUser.ID, OType.STRING).oIndex(OClass.INDEX_TYPE.UNIQUE).notNull()
				.oProperty(ICOFarmUser.RESTORE_ID, OType.STRING).switchDisplayable(false)
				.oProperty(ICOFarmUser.RESTORE_ID_CREATED, OType.DATETIME).switchDisplayable(false).getOClass();

		updateUserCustomAttributes(user);

	}

	private void updateDefaultOrientDbUsers(ODatabaseDocument db) {
		Consumer<ODocument> update = (doc) -> {
			doc.field(ICOFarmUser.EMAIL, UUID.randomUUID().toString() + "@gmail.com");
			doc.field(ICOFarmUser.ID, UUID.randomUUID().toString());
			doc.save();
		};
		OSecurity security = db.getMetadata().getSecurity();

		update.accept(security.getUser("admin").getDocument());
		update.accept(security.getUser("reader").getDocument());
		update.accept(security.getUser("writer").getDocument());
	}

	private void adjustDashboard(OSchemaHelper helper) {
		Function<String , ODocument> dashboardFactory = (className) -> helper.oClass(OWidgetsModule.OCLASS_DASHBOARD)
				.oDocument()
				.field(OWidgetsModule.OPROPERTY_DOMAIN, "browse")
				.field(OWidgetsModule.OPROPERTY_TAB, "list")
				.field(OWidgetsModule.OPROPERTY_CLASS, className).saveDocument().getODocument();

		BiConsumer<String, String> createWidget = (typeId, className) -> {
			ODocument dashboard = dashboardFactory.apply(className);
			helper.oClass(OWidgetsModule.OCLASS_WIDGET)
					.oDocument()
					.field(OWidgetsModule.OPROPERTY_TYPE_ID, typeId)
					.field(OWidgetsModule.OPROPERTY_DASHBOARD, dashboard)
					.saveDocument().getODocument();
		};

		createWidget.accept(REGISTRATION, REGISTRATION);
		createWidget.accept(REFERRAL_WIDGET_ID, REFERRAL);
    }

	@Override
	public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
		onInstall(app, db);
	}

	@Override
	public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
		initHiddenProperties();
		initHiddenWidgets();
	}

	private void initHiddenProperties() {
		List<String> userProperties = new ArrayList<>(9);
		userProperties.add(ICOFarmUser.ID);
		userProperties.add(ICOFarmUser.RESTORE_ID);
		userProperties.add(ICOFarmUser.RESTORE_ID_CREATED);
		userProperties.add("name");
		userProperties.add("online");
		userProperties.add("status");
		userProperties.add("lastSessionId");
		userProperties.add("roles");

		List<String> restrictedProperties = new ArrayList<>(4);
		restrictedProperties.add(ORESTRICTED_ALLOW);
		restrictedProperties.add(ORESTRICTED_ALLOW_READ);
		restrictedProperties.add(ORESTRICTED_ALLOW_UPDATE);
		restrictedProperties.add(ORESTRICTED_ALLOW_DELETE);

		List<String> identityProperties = new ArrayList<>(2);
		identityProperties.add("perspective");
		identityProperties.add("perspectiveItem");

		HIDDEN_PROPERTIES.put(ICOFarmUser.CLASS_NAME, userProperties);
		HIDDEN_PROPERTIES.put("ORestricted", restrictedProperties);
		HIDDEN_PROPERTIES.put(OIdentity.CLASS_NAME, identityProperties);
	}

	private void initHiddenWidgets() {
		HIDDEN_WIDGETS.put(REFERRAL, Collections.singletonList("list-all"));
	}
}
