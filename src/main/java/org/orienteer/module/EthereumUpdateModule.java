package org.orienteer.module;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDbService;
import org.orienteer.service.IEthereumService;
import org.orienteer.service.IUpdateWalletService;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EthereumUpdateModule extends AbstractOrienteerModule {

    @Inject
    private IEthereumService ethereumService;

    private ScheduledFuture<?> future;

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
        ethereumService.init();
        IDbService dbService = app.getServiceInstance(IDbService.class);
        IUpdateWalletService updateService = app.getServiceInstance(IUpdateWalletService.class);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        future = executor.scheduleAtFixedRate(() -> {
            List<Wallet> wallets = dbService.getWallets();
            updateService.update(wallets);
        }, 0, ethereumService.getConfig().getTimeout(), TimeUnit.MINUTES);
    }

    @Override
    public void onDestroy(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
        super.onDestroy(app, db, moduleDoc);
        future.cancel(true);
    }
}
