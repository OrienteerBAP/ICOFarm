package org.orienteer.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import org.orienteer.model.EthereumClientConfig;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;

public class ICOFarmInitModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public Web3j provideWeb3j(Web3jService service) {
        return Web3j.build(service);
    }

    @Provides
    @Singleton
    public Web3jService provideWeb3jService(EthereumClientConfig config) {
        return new HttpService(config.getHost() + ":" + config.getPort());
    }

    @Provides
    @Singleton
    public EthereumClientConfig provideEthereumClientConfig(IDbService dbService) {
        ODatabaseDocumentInternal db = ODatabaseRecordThreadLocal.instance().getIfDefined();
        EthereumClientConfig config = db != null && db.exists() ? dbService.getEthereumClientConfig() : null;
        if (config == null) {
            config = new EthereumClientConfig.Builder()
                    .setHost("http://localhost")
                    .setPort(8545)
                    .setName("default")
                    .setTimeout(15)
                    .setWorkFolder("icofarm")
                    .build();
        }
        return config;
    }
}
