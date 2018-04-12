package org.orienteer.method;

import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.orienteer.component.BuyTokenPopupPanel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.EthereumWallet;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.TokenCurrency;
import org.orienteer.service.Buyable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@OMethod(
		icon=FAIconType.dollar,
		filters={
				@OFilter(fClass = ODocumentFilter.class, fData = "TokenCurrency"),
			@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)

public class BuyToken extends AbstractModalOMethod {
	private static final long serialVersionUID = 1L;
	
	private static final BigInteger GAS_PRICE = Convert.toWei(BigDecimal.ONE, Convert.Unit.GWEI).toBigInteger();
	private static final BigInteger GAS_LIMIT = BigInteger.valueOf(200000);
	private static final Logger LOG = LoggerFactory.getLogger(BuyToken.class);
	
	

	@Override
	public Component getModalContent(String componentId, ModalWindow modal,AbstractModalWindowCommand<?> command) {
		return new BuyTokenPopupPanel(componentId, modal,command) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean onSubmitForm(AjaxRequestTarget target) {
				boolean Ok=false;
				String walletAddress="";
				try {
					String walletFile = doSaveWalletFile();
					
					IModel<String> password = getWalletPassword();
					IModel<String> summ = getEthSumm();
					String tokenAddress = getTokenCurrency().getContractAddress();

					walletAddress = doBuyTokens(walletFile,password.getObject(),tokenAddress, new BigDecimal(summ.getObject()));
					//https://rinkeby.etherscan.io/address/0xf8f3d3d326c78f0d274f91f2428305a89002660e
					//AbstractWidgetDisplayModeAwarePage<ODocument> page = new ODocumentPage(new ODocumentModel(session.getOTaskSessionPersisted().getDocument())).setModeObject(DisplayMode.VIEW);
					Ok = true;
				} catch (Exception e) {
					error(e.getMessage()+" ");
				}
				if (Ok){
					throw new RedirectToUrlException("https://rinkeby.etherscan.io/address/"+walletAddress);
				}
				return Ok;
			}

			@Override
			public SimpleNamingModel<String> getButtonTitle() {
				return getTitleModel();
			}

		};
	}
	
	protected EthereumWallet getWallet() throws Exception{
		OSecurityUser user = OrienteerWebSession.get().getUser();
		
		if (user==null)	throw new Exception("Please autorize");
		ICOFarmUser icofarmUser = new ICOFarmUser(user.getDocument());
		
		EthereumWallet wallet = icofarmUser.getMainETHWallet();
		if (wallet==null) throw new Exception("Please link correct ETC wallet to your account");
		return wallet;
	}
	
	protected TokenCurrency getTokenCurrency() throws Exception{
		IModel<?> currencyModel = getEnvData().getDisplayObjectModel();
		ODocument currencyDoc = (ODocument) currencyModel.getObject();
		if (currencyDoc==null) throw new Exception("Please link buy button to 'currency' OClass");
		return new TokenCurrency(currencyDoc);		
	}
	
	private String doSaveWalletFile() throws Exception{

		EthereumWallet wallet = getWallet();
		
		String walletSource = wallet.getWalletJSON();
		if (walletSource==null) throw new Exception("Please set correct ETC wallet JSON");
		
		File file = new File(EthereumWallet.CACHE_FOLDER+"/"+wallet.getWalletJSONName());
		file.getParentFile().mkdirs(); 
		file.createNewFile();
        FileWriter fw = new FileWriter(file);
		fw.write(walletSource);
        fw.close();
        return file.getAbsolutePath();
	}
	
	private String doBuyTokens(String wallet,String password,String contractAddress,BigDecimal ETHquantity) throws Exception{
		Web3j web3 = Web3j.build(new HttpService("https://rinkeby.infura.io/jQ5uVScqyIMjgP6ZSSMb"));
		Credentials credentials = WalletUtils.loadCredentials(password, wallet);
		Buyable token = Buyable.load(contractAddress, web3, credentials, GAS_PRICE, GAS_LIMIT);
		CompletableFuture<TransactionReceipt> result = token.buy(ETHquantity.toBigInteger()).sendAsync();
		return credentials.getAddress();
	}

}

