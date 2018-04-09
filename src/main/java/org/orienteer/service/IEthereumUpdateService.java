package org.orienteer.service;

import com.google.inject.ImplementedBy;

@ImplementedBy(EthereumUpdateServiceImpl.class)
public interface IEthereumUpdateService {
    public void init();
    public void destroy();
}
