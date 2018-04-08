package org.orienteer.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
    public Web3jService provideWeb3jService() {
        return new HttpService();
    }
}
