package org.orienteer.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.security.OIdentity;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.commons.collections4.map.HashedMap;
import org.orienteer.component.visualizer.HashVisualizer;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.visualizer.UIVisualizersRegistry;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.core.module.PerspectivesModule;
import org.orienteer.core.util.OSchemaHelper;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OTransaction;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;

import java.util.*;

import static org.orienteer.core.module.OWidgetsModule.*;
import static org.orienteer.module.ICOFarmModule.*;
import static org.orienteer.module.ICOFarmSecurityModule.*;

public class ICOFarmPerspectiveModule extends AbstractOrienteerModule {

    public static final String INVESTOR_PERSPECTIVE_KEY  = "perspective.name.investor";
    public static final String ANONYMOUS_PERSPECTIVE_KEY = "perspective.name.anonymous";

    public static final String REFERRAL_WIDGET_ID                = "referrals-widget";
    public static final String REGISTRATION_WIDGET_ID            = "registration";
    public static final String SCHEMA_CLASSES_WIDGET_ID          = "list-oclasses";
    public static final String LIST_DOCUMENTS_WIDGET_ID          = "list-all";
    public static final String WALLETS_WIDGET_ID                 = "wallets-widget";
    public static final String BUY_TOKENS_WIDGET_ID              = "buy-tokens-widget";
    public static final String TRANSFER_TOKENS_WIDGET_ID         = "transfer-tokens-widget";
    public static final String WALLET_BALANCE_WIDGET_ID          = "balance-widget";
    public static final String WALLET_TRANSACTIONS_WIDGET_ID     = "wallet-transactions-widget";
    public static final String TOKEN_TRANSACTIONS_WIDGET_ID      = "token-transactions-widget";
    public static final String TOKEN_TRANSACTIONS_INFO_WIDGET_ID = "token-transactions-info";
    public static final String TOKEN_MONEY_WIDGET_ID             = "token-money-widget";
    public static final String MONEY_WIDGET_ID                   = "money-widget";
    public static final String USER_STATISTICS_WIDGET_ID         = "user-statistics-widget";
    public static final String USER_INVESTORS_WIDGET_ID          = "investors-widget";

    public static final String WALLET_BALANCE_TAB = "Balance";
    public static final String WALLET_TRANSACTIONS_TAB = "Transactions";

    public static final String TOKEN_TRANSACTIONS_TAB = "Transactions";
    public static final String TOKEN_MONEY_TAB        = "Money";

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

    protected ICOFarmPerspectiveModule() {
        super("icofarm-perspective", ICOFarmModule.VERSION);
    }

    @Override
    public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
        OSchemaHelper helper = OSchemaHelper.bind(db);
        adjustPerspectives(helper);
        updatePerspectivesPermissions(helper, db);
        adjustDashboard(helper);
        return null;
    }

    @Override
    public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
        onInstall(app, db);
    }

    @Override
    public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db) {
        initHiddenProperties();
        initHiddenWidgets();
        registerVisualizers(app);
    }

    private void adjustPerspectives(OSchemaHelper helper) {
        List<String> languageTags = createDefaultLanguageTags();

        adjustInvestorPerspective(helper, languageTags);
        adjustAnonymousPerspective(helper, languageTags);
        adjustDefaultPerspective(helper, languageTags);
    }

    private void adjustInvestorPerspective(OSchemaHelper helper, List<String> languageTags) {
        ODocument perspective = getOrCreatePerspective(INVESTOR_PERSPECTIVE_KEY, helper, languageTags);
        perspective.field("icon", FAIconType.usd.name());
        perspective.field("homeUrl", "/browse/" + Wallet.CLASS_NAME);
        perspective.save();

        ODocument item1 = getOrCreatePerspectiveItem("perspective.menu.item.wallets", perspective, helper, languageTags);
        item1.field("icon", FAIconType.briefcase.name());
        item1.field("perspective", perspective);
        item1.field("url", "/browse/" + Wallet.CLASS_NAME);
        item1.save();

        ODocument item2 = getOrCreatePerspectiveItem("perspective.menu.item.tokens", perspective, helper, languageTags);
        item2.field("icon", FAIconType.money.name());
        item2.field("perspective", perspective);
        item2.field("url", "/browse/" + Token.CLASS_NAME);
        item2.save();

        ODocument item3 = getOrCreatePerspectiveItem("perspective.menu.item.buyTokens", perspective, helper, languageTags);
        item3.field("icon", FAIconType.dollar.name());
        item3.field("perspective", perspective);
        item3.field("url", "/browse/" + BUY_TOKENS);
        item3.save();

        ODocument item4 = getOrCreatePerspectiveItem("perspective.menu.item.transferTokens", perspective, helper, languageTags);
        item4.field("icon", FAIconType.exchange.name());
        item4.field("perspective", perspective);
        item4.field("url", "/browse/" + TRANSFER_TOKENS);
        item4.save();

        ODocument item5 = getOrCreatePerspectiveItem("perspective.menu.item.referrals", perspective, helper, languageTags);
        item5.field("icon", FAIconType.users.name());
        item5.field("perspective", perspective);
        item5.field("url", "/browse/" + REFERRAL);
        item5.save();

        ODocument item6 = getOrCreatePerspectiveItem("perspective.menu.item.transactions", perspective, helper, languageTags);
        item6.field("icon", FAIconType.align_justify.getCssClass());
        item6.field("perspective", perspective);
        item6.field("url", "/browse/" + OTransaction.CLASS_NAME);
        item6.save();

        perspective.field("menu", Arrays.asList(item1, item2, item3, item4, item5, item6));
        perspective.save();

    }

    private void adjustAnonymousPerspective(OSchemaHelper helper, List<String> languageTags) {
        ODocument perspective = getOrCreatePerspective(ANONYMOUS_PERSPECTIVE_KEY, helper, languageTags);
        perspective.field("icon", FAIconType.user_secret.name());
        perspective.field("homeUrl", "/browse/Registration");
        perspective.save();

        ODocument item1 = getOrCreatePerspectiveItem("perspective.menu.item.registration", perspective, helper, languageTags);
        item1.field("icon", FAIconType.info.name());
        item1.field("perspective", perspective);
        item1.field("url", "/browse/Registration");
        item1.save();

        perspective.field("menu", Collections.singletonList(item1));
        perspective.save();
    }

    private void adjustDefaultPerspective(OSchemaHelper helper, List<String> languageTags) {
        PerspectivesModule perspectivesModule = OrienteerWebApplication.get().getServiceInstance(PerspectivesModule.class);
        ODocument perspective = perspectivesModule.getPerspectiveByName(helper.getDatabase(), PerspectivesModule.DEFAULT_PERSPECTIVE);

        ODocument item1 = getOrCreatePerspectiveItem("perspective.menu.item.tokens", perspective, helper, languageTags);
        item1.field("icon", FAIconType.money.name());
        item1.field("perspective", perspective);
        item1.field("url", "/browse/" + Token.CLASS_NAME);
        item1.save();

        ODocument item2 = getOrCreatePerspectiveItem("perspective.menu.item.transactions", perspective, helper, languageTags);
        item2.field("icon", FAIconType.align_justify.getCssClass());
        item2.field("perspective", perspective);
        item2.field("url", "/browse/" + OTransaction.CLASS_NAME);
        item2.save();

        ODocument item3 = getOrCreatePerspectiveItem("perspective.menu.item.money", perspective, helper, languageTags);
        item3.field("icon", FAIconType.dollar.name());
        item3.field("perspective", perspective);
        item3.field("url", "/browse/" + ICOFarmModule.MONEY);
        item3.save();
    }

    private void updatePerspectivesPermissions(OSchemaHelper helper, ODatabaseDocument db) {
        OClass restricted = helper.oClass("ORestricted").getOClass();
        OClass perspectiveClass = helper.oClass(PerspectivesModule.OCLASS_PERSPECTIVE).getOClass();

        if (!perspectiveClass.isSubClassOf(restricted)) {
            perspectiveClass.addSuperClass(restricted);
        }
        updateUser(db, ANONYMOUS_PERSPECTIVE_KEY, ORESTRICTED_ALLOW_READ, "reader");
        updateRole(db, INVESTOR_PERSPECTIVE_KEY, ORESTRICTED_ALLOW_READ, INVESTOR_ROLE);
    }

    private void adjustDashboard(OSchemaHelper helper) {
        createWidgetIfNotExists(REGISTRATION_WIDGET_ID, REGISTRATION, helper);

        createWidgetIfNotExists(BUY_TOKENS_WIDGET_ID, BUY_TOKENS, helper);
        createWidgetIfNotExists(TRANSFER_TOKENS_WIDGET_ID, TRANSFER_TOKENS, helper);

        createWidgetIfNotExists(REFERRAL_WIDGET_ID, REFERRAL, helper);
        createWidgetIfNotExists(LIST_DOCUMENTS_WIDGET_ID, REFERRAL, helper);

        createWidgetIfNotExists(WALLETS_WIDGET_ID, Wallet.CLASS_NAME, helper);
        createWidgetIfNotExists(LIST_DOCUMENTS_WIDGET_ID, Wallet.CLASS_NAME, helper);

        createWidgetIfNotExists(MONEY_WIDGET_ID, ICOFarmModule.MONEY, helper);
    }

    private void initHiddenProperties() {
        List<String> userProperties = new ArrayList<>(9);
        userProperties.add(ICOFarmUser.OPROPERTY_ID);
        userProperties.add(ICOFarmUser.OPROPERTY_RESTORE_ID);
        userProperties.add(ICOFarmUser.ORPOPERTY_RESTORE_ID_CREATED);
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

        List<String> walletProperties = new ArrayList<>(1);
        walletProperties.add(Wallet.OPROPERTY_OWNER);

        List<String> tokenProperties = new ArrayList<>(1);
        tokenProperties.add(Token.OPROPERTY_OWNER);

        HIDDEN_PROPERTIES.put(ICOFarmUser.CLASS_NAME, userProperties);
        HIDDEN_PROPERTIES.put("ORestricted", restrictedProperties);
        HIDDEN_PROPERTIES.put(OIdentity.CLASS_NAME, identityProperties);
        HIDDEN_PROPERTIES.put(Wallet.CLASS_NAME, walletProperties);
        HIDDEN_PROPERTIES.put(Token.CLASS_NAME, tokenProperties);
    }

    private void initHiddenWidgets() {
        HIDDEN_WIDGETS.put(REFERRAL, Collections.singletonList(LIST_DOCUMENTS_WIDGET_ID));
        HIDDEN_WIDGETS.put(Wallet.CLASS_NAME, Collections.singletonList(LIST_DOCUMENTS_WIDGET_ID));
        HIDDEN_WIDGETS.put(Token.CLASS_NAME, Arrays.asList(TOKEN_MONEY_WIDGET_ID, TOKEN_TRANSACTIONS_INFO_WIDGET_ID));
    }

    private void registerVisualizers(OrienteerWebApplication app) {
        UIVisualizersRegistry registry = app.getUIVisualizersRegistry();
        registry.registerUIComponentFactory(new HashVisualizer());
    }

    private ODocument getOrCreatePerspective(String key, OSchemaHelper helper, List<String> tags) {
        OrienteerWebApplication app = OrienteerWebApplication.get();
        Map<String, String> localizedStrings = getLocalizedStrings(app, key, tags);
        PerspectivesModule perspectivesModule = app.getServiceInstance(PerspectivesModule.class);
        ODocument doc = perspectivesModule.getPerspectiveByName(helper.getDatabase(), localizedStrings.get("en"));
        if (doc == null) {
            doc = new ODocument(PerspectivesModule.OCLASS_PERSPECTIVE);
            doc.field("name", localizedStrings);
        }
        return doc;
    }

    private ODocument getOrCreatePerspectiveItem(String key, ODocument perspective, OSchemaHelper helper, List<String> tags) {
        Map<String, String> localizedStrings = getLocalizedStrings(OrienteerWebApplication.get(), key, tags);
        List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>("select from " + PerspectivesModule.OCLASS_ITEM
                + " where name.values() contains ? and perspective = ?"), localizedStrings.get("en"), perspective);
        ODocument doc = docs != null && !docs.isEmpty() ? docs.get(0) : null;
        if (doc == null) {
            doc = new ODocument(PerspectivesModule.OCLASS_ITEM);
            doc.field("name", localizedStrings);
        }
        return doc;
    }

    private void createWidgetIfNotExists(String typeId, String className, OSchemaHelper helper) {
        ODocument dashboard = getOrCreateDashboard(className, helper);
        if (!isWidgetExists(dashboard, typeId)) {
           ODocument doc = new ODocument(OCLASS_WIDGET);
           doc.field(OPROPERTY_TYPE_ID, typeId);
           doc.field(OPROPERTY_DASHBOARD, dashboard);
           doc.save();
        }
    }

    private ODocument getOrCreateDashboard(String className, OSchemaHelper helper) {
        String sql = String.format("select from %s where %s = ?", OCLASS_DASHBOARD, OPROPERTY_CLASS);
        List<ODocument> docs = helper.getDatabase().query(new OSQLSynchQuery<>(sql, 1), className);
        ODocument doc;
        if (docs == null || docs.isEmpty()) {
            doc = new ODocument(OCLASS_DASHBOARD);
            doc.field(OPROPERTY_DOMAIN, "browse");
            doc.field(OPROPERTY_TAB, "list");
            doc.field(OPROPERTY_CLASS, className);
            doc.save();
        } else doc = docs.get(0);

        return doc;
    }

    private boolean isWidgetExists(ODocument dashboard, String typeId) {
        List<OIdentifiable> widgets = (List<OIdentifiable>) dashboard.field(OPROPERTY_WIDGETS);
        return widgets != null && !widgets.isEmpty() && widgets.stream()
                .anyMatch(widget -> typeId.equals(((ODocument)widget.getRecord()).field(OPROPERTY_TYPE_ID)));
    }

    private void updateRole(ODatabaseDocument db, String key, String field, String roleName) {
        OSecurity security = db.getMetadata().getSecurity();
        ODocument perspective = getPerspectiveByNameKey(db, key);
        ORole role = security.getRole(roleName);
        role.getDocument().field("perspective", perspective);
        perspective.field(field, Collections.singletonList(role.getDocument()));
        role.save();
        perspective.save();
    }

    private void updateUser(ODatabaseDocument db, String key, String field, String username) {
        OSecurity security = db.getMetadata().getSecurity();
        ODocument perspective = getPerspectiveByNameKey(db, key);
        OUser user = security.getUser(username);
        user.getDocument().field("perspective", perspective);
        perspective.field(field, Collections.singletonList(user.getDocument()));
        user.save();
        perspective.save();
    }

    private ODocument getPerspectiveByNameKey(ODatabaseDocument db, String key) {
        OrienteerWebApplication app = OrienteerWebApplication.get();
        PerspectivesModule perspectivesModule = app.getServiceInstance(PerspectivesModule.class);
        return perspectivesModule.getPerspectiveByName(db, getLocalizedStrings(app, key, createDefaultLanguageTags()).get("en"));
    }

    private List<String> createDefaultLanguageTags() {
        return Arrays.asList("en", "ru", "uk");
    }

    private Map<String, String> getLocalizedStrings(OrienteerWebApplication app, String key, List<String> tags) {
        Map<String, String> localized = new HashedMap<>(3);
        for (String tag : tags) {
            localized.put(tag, getString(app, key, Locale.forLanguageTag(tag)));
        }
        return localized;
    }

    private String getString(OrienteerWebApplication app, String key, Locale locale) {
        return app.getResourceSettings().getLocalizer().getString(key, null, null, locale, null, "");
    }
}
