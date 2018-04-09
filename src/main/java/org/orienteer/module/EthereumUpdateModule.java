package org.orienteer.module;

import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.module.AbstractOrienteerModule;

public class EthereumUpdateModule extends AbstractOrienteerModule {



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
//        ethereumService.init();
//        IDbService dbService = app.getServiceInstance(IDbService.class);
//        IEthereumUpdateService updateService = app.getServiceInstance(IEthereumUpdateService.class);
//        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
//
//        future = executor.scheduleAtFixedRate(() -> {
//            ThreadContext.setApplication(app);
//            List<Wallet> wallets = dbService.getWallets();
//            updateService.update(wallets);
//        }, 0, ethereumService.getConfig().getTimeout(), TimeUnit.MINUTES);

    }

//    private Subscriber<Transaction> subscribeOnTransactions(OrienteerWebApplication app, ODatabaseDocument db) {
//        return ethereumService.getTransactionObservable().filter(t -> dbServic)
//    }
//
//    private Completable updateBalanceInWallets(OrienteerWebApplication app, ODatabaseDocument db) {
//
//    }

    @Override
    public void onDestroy(OrienteerWebApplication app, ODatabaseDocument db, ODocument moduleDoc) {
        super.onDestroy(app, db, moduleDoc);
//        future.cancel(true);
    }
}
