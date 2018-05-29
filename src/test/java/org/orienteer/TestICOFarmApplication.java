package org.orienteer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.junit.OrienteerTester;
import org.orienteer.service.web3.ERC20Interface;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSyncing;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.assertTrue;


@RunWith(OrienteerTestRunner.class)
@Singleton
public class TestICOFarmApplication
{
	@Inject
	private OrienteerTester tester;
    
	@Test
	public void testWebApplicationClass()
	{
	    assertTrue(tester.getApplication() instanceof ICOFarmApplication);
	}
	
	//@Test
	public void ethereumTest(){
		//Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/jQ5uVScqyIMjgP6ZSSMb"));
		Web3j web3 = Web3j.build(new HttpService());
		//Web3j web3 = Web3j.build(new WindowsIpcService("/root/.ethereum/rinkeby/geth.ipc"));
		Web3ClientVersion web3ClientVersion=null;
		try {
			web3ClientVersion = web3.web3ClientVersion().send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String clientVersion = web3ClientVersion.getWeb3ClientVersion();

		EthSyncing isSyncing= null;
		try {
			isSyncing = web3.ethSyncing().send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EthBlockNumber blockNumber = null;
		try {
			blockNumber = web3.ethBlockNumber().send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*

        try {
			String walletFileName = WalletUtils.generateFullNewWalletFile("123qweasdzxc",
			        new File("D:/work/wallet/test"));
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException
				| CipherException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
*/
		String metamaskAccount = "0xDF249CbC3d326225741f5e81365d10566025e1e6";
		String lastContract = "0xc70C84B7Df2007099A3BB4B23ae9B8D88d11cF49";
		Credentials credentials;
		BigInteger contractBalance=BigInteger.ZERO;
		String owner = "";
		try {
//		    BigInteger privateKeyInBT = new BigInteger("0b2e9e51e711201cef6d16107c69feb48e4755540e3a24cd903ee7ece05c1380", 16);
//		    ECKeyPair aPair = ECKeyPair.create(privateKeyInBT);
//			credentials = Credentials.create(aPair);
			credentials = WalletUtils.loadCredentials("123qweasdzxc", "D:/work/wallet/etc_rinkeby/UTC--2018-03-29T09-52-29.848Z--f8f3d3d326c78f0d274f91f2428305a89002660e");
			/*
			TransactionReceipt transactionReceipt = Transfer.sendFunds(
			        web3, credentials, metamaskAccount,
			        BigDecimal.valueOf(500.0), Convert.Unit.GWEI)
			        .send();
			        */
			/*
			TestStorage contract = TestStorage.deploy(
					web3, credentials, Convert.toWei(BigDecimal.ONE, Convert.Unit.GWEI).toBigInteger() , BigInteger.valueOf(4700000)).send();
			 * */
			//Convert.Unit.GWEI
			BigInteger gasPrice = Convert.toWei(BigDecimal.ONE, Convert.Unit.GWEI).toBigInteger();
			BigInteger gasLimit = BigInteger.valueOf(4700000);
			
//			TestStorage contract = TestStorage.load(lastContract, web3, credentials, gasPrice, gasLimit);
//			contract.addFounds(Convert.toWei(BigDecimal.valueOf(111), Convert.Unit.GWEI).toBigInteger()).send();
			//contractBalance = contract.balances("0xF8f3d3D326c78F0d274f91f2428305a89002660e").send();
			//contract.withdrawFounds().send();

			//owner = contract.owner().send();
			String erc20Contract = "0xc1b0A0a6672939094Db09a0F446bD10A0C833d9D";
			
			ERC20Interface token = ERC20Interface.load(erc20Contract, web3, credentials, gasPrice, gasLimit);
			//token.transfer(erc20Contract, BigInteger.valueOf(50)).send();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				

		EthGetBalance ethGetBalance=null;

		try {
			ethGetBalance = web3
					  .ethGetBalance("0xF8f3d3D326c78F0d274f91f2428305a89002660e", DefaultBlockParameterName.LATEST)
					  .send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger wei = ethGetBalance.getBalance();

		EthGetBalance ethGetBalance2=null;

		try {
			ethGetBalance2 = web3
					  .ethGetBalance("0xD560986CC5e8e88B0D9b1Ea71aD7B637c458c48a", DefaultBlockParameterName.LATEST)
					  .send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger wei2 = ethGetBalance2.getBalance();

		EthGetBalance ethGetBalancemm = null;
		try {
			ethGetBalancemm = web3
					  .ethGetBalance(metamaskAccount, DefaultBlockParameterName.LATEST)
					  .send();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BigInteger weimm = ethGetBalancemm.getBalance();
		
		System.out.println("----------------------------------------------------------");
		System.out.println(clientVersion);
		System.out.println(isSyncing.getRawResponse());
		System.out.println(blockNumber.getRawResponse());
		
		System.out.println(wei.toString());
		System.out.println(wei2.toString());
		System.out.println(weimm.toString());
		System.out.println(contractBalance.toString());
		System.out.println(owner);
		
		System.out.println("----------------------------------------------------------");
	}
}
