package org.orienteer;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.function.BiConsumer;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

@RunWith(OrienteerTestRunner.class)
public class TestICOFarmEthereumService {

    @Inject
    private IEthereumService service;

    @Inject
    private IDBService dbService;

    @Test
    public void testCreateWallet() throws Exception {
        String password = "qwerty";
        byte[] wallet = service.createWallet(password);
        assertNotNull(wallet);
        Credentials credentials = service.readWallet(password, wallet);
        assertNotNull(credentials);
        assertNotNull(credentials.getAddress());
        assertNotNull(credentials.getEcKeyPair());
    }

    @Test
    public void testGetBalance() {
        String address = "0x610804919c1adb474c24cc9ea05c1c0949dfb919"; // Test address on rinkeby
        service.requestBalanceAsync(address, (err, balance) -> {
            assertNull(err);
            assertNotNull(balance);
        });
    }

    @Test
    public void testErrorGetBalance() {
        service.requestBalanceAsync("123", (err, balance) -> {
            assertNotNull(err);
            assertNull(balance);
        });
    }

    @Test
    public void testRequestBalance() throws InterruptedException {
        BiConsumer<Exception, BigInteger> balanceConsumer = (err, balance) -> {
            assertNull(err);
            assertNotNull(balance);
        };
        dbService.getWallets().forEach((wallet) -> {
            assertNotNull(wallet.getAddress());
            service.requestBalanceAsync(wallet.getAddress(), balanceConsumer);
        });

        Thread.currentThread().join(5000);
    }
}
