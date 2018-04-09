package org.orienteer.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.model.EmbeddedWallet;
import org.orienteer.service.IDbService;
import org.orienteer.service.IUpdateWalletService;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EthereumUpdateModule extends AbstractOrienteerModule {

    public static final String OPROPERTY_TIMEOUT = "timeout";

    protected EthereumUpdateModule() {
        super("ethereum-update", ICOFarmModule.VERSION);
    }

    @Override
    public ODocument onInstall(OrienteerWebApplication app, ODatabaseDocument db) {
        return super.onInstall(app, db);
    }

    @Override
    public void onUpdate(OrienteerWebApplication app, ODatabaseDocument db, int oldVersion, int newVersion) {
        onInstall(app, db);
    }

    @Override
    public void onInitialize(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
        super.onInitialize(app, db, moduleDoc);
        IDbService dbService = app.getServiceInstance(IDbService.class);
        IUpdateWalletService updateService = app.getServiceInstance(IUpdateWalletService.class);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        executor.scheduleAtFixedRate(() -> {
            List<EmbeddedWallet> wallets = dbService.getEmbeddedWallets();
            updateService.updateBalance(wallets);
        }, 0, 1, TimeUnit.MINUTES); // TODO: adjust timeout
    }

    @Override
    public void onDestroy(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
        super.onDestroy(app, db, moduleDoc);
    }
}
