package org.orienteer;

import com.google.inject.Inject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.service.web3.IEthereumService;
import org.orienteer.service.web3.IICOFarmSmartContract;
import org.web3j.crypto.Credentials;
import rx.observers.AssertableSubscriber;

import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;

@RunWith(OrienteerTestRunner.class)
public class TestICOFarmEthereumService {

    private static Wallet wallet;
    private static String password;

    private static Token testToken;

    @Inject
    private IEthereumService ethService;

    @Inject
    private IDBService dbService;


    @BeforeClass
    public static void init() throws Exception {
        String fileName = "UTC--2018-04-07T11-35-17.644352709Z--610804919c1adb474c24cc9ea05c1c0949dfb919";
        URL url = TestICOFarmEthereumService.class.getResource(fileName);
        byte [] json = Files.readAllBytes(Paths.get(url.toURI()));
        wallet = new Wallet();
        wallet.setAddress("0x610804919c1adb474c24cc9ea05c1c0949dfb919");
        wallet.setWalletJSON(json);
        password = "1WrcOCTQIQH28iOX";

        testToken = new Token();
        testToken.setAddress("0xa4613F269117EE521717921Ed4EDaAdcfdfa6FAC");
    }


    @Test
    public void testCreateWallet() throws Exception {
        String password = "qwerty";
        byte[] wallet = ethService.createWallet(password);
        assertNotNull(wallet);
        Credentials credentials = ethService.readWallet(password, wallet).toBlocking().value();
        assertNotNull(credentials);
        assertNotNull(credentials.getAddress());
        assertNotNull(credentials.getEcKeyPair());
    }

    @Test
    public void testGetBalance() {
        String address = "0x610804919c1adb474c24cc9ea05c1c0949dfb919"; // Test address on rinkeby
        ethService.requestBalanceAsync(address, (err, balance) -> {
            assertNull(err);
            assertNotNull(balance);
        });
    }

    @Test
    public void testErrorGetBalance() {
        ethService.requestBalanceAsync("123", (err, balance) -> {
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
            ethService.requestBalanceAsync(wallet.getAddress(), balanceConsumer);
        });

        Thread.currentThread().join(5000);
    }

    @Test
    public void testRequestTokenBalance() throws Exception {
        AssertableSubscriber<BigInteger> test = ethService.loadSmartContract(wallet.getAddress(), testToken)
                .getBalance().test();
        test.assertNoErrors();
        test.assertCompleted();
        test.awaitTerminalEventAndUnsubscribeOnTimeout(10, TimeUnit.SECONDS);

        Thread.currentThread().join(10_000);
    }

    @Test
    public void testEstimateGasCost() throws Exception {
        IICOFarmSmartContract smartContract = ethService.loadSmartContract(wallet.getAddress(), testToken);
        AssertableSubscriber<BigInteger> test = smartContract.estimateGasForBuy(new BigInteger("1000000000")).test();//36854
        test.assertNoErrors();
        test.assertCompleted();
        test.assertValue(new BigInteger("36854"));
        test.awaitTerminalEventAndUnsubscribeOnTimeout(5, TimeUnit.SECONDS);
        Thread.currentThread().join(5_000);
    }
}
