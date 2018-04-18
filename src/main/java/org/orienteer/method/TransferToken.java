package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.orienteer.ICOFarmApplication;
import org.orienteer.component.TransferTokenPopupPanel;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.utils.Convert;
import ru.ydn.wicket.wicketorientdb.model.SimpleNamingModel;

import java.math.BigDecimal;
import java.math.BigInteger;

@OMethod(
		icon = FAIconType.external_link,
		titleKey = "withdraw",
		filters = {
			@OFilter(fClass = ODocumentFilter.class, fData = "Wallet"),
			@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)
public class TransferToken extends AbstractModalOMethod {
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(BuyToken.class);

	// TODO: refactor static fields
	private static final BigInteger GAS_PRICE = Convert.toWei(BigDecimal.ONE, Convert.Unit.GWEI).toBigInteger();
	private static final BigInteger GAS_LIMIT = BigInteger.valueOf(200000);
	
	@Override
	public Component getModalContent(String componentId, ModalWindow modal,AbstractModalWindowCommand<?> command) {
		return new TransferTokenPopupPanel(componentId, modal,command) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean onSubmitForm(AjaxRequestTarget target) {
				try {
					IModel<String> password = getWalletPassword();
					IModel<String> summ = getTokenQuantity();
					IModel<String> targetWallet = getTargetWalletAddress();
					String tokenAddress = getTokenCurrency().getContractAddress();
					Credentials credentials = getCredentials(password.getObject(), getWallet());

					getEthereumService().transferTokens(credentials, tokenAddress, targetWallet.getObject(), new BigInteger(summ.getObject()), GAS_PRICE, GAS_LIMIT);
					//https://rinkeby.etherscan.io/address/0xf8f3d3d326c78f0d274f91f2428305a89002660e
					//AbstractWidgetDisplayModeAwarePage<ODocument> page = new ODocumentPage(new ODocumentModel(session.getOTaskSessionPersisted().getDocument())).setModeObject(DisplayMode.VIEW);
					return true;
				} catch (Exception e) {
					LOG.error("Can't transfer tokens!", e);
					error(e.getMessage()+" ");
				}
				return false;
			}

			@Override
			public SimpleNamingModel<String> getButtonTitle() {
				return getTitleModel();
			}

		};
	}

	protected Wallet getWallet() throws Exception {
		IModel<?> walletModel = getContext().getDisplayObjectModel();
		ODocument walletDoc = (ODocument) walletModel.getObject();
		if (walletDoc == null) {
			throw new Exception("Please link buy button to 'EthereumWallet' OClass");
		}
		return new Wallet(walletDoc);
		
	}
	
	protected TokenCurrency getTokenCurrency() throws Exception {
		TokenCurrency tokenCurrency = getConfig().getMainTokenCurrency(); 
		if (tokenCurrency.getDocument() == null) {
			throw new Exception("Please set main token currency in ICOFarm module settings!");
		}
		return tokenCurrency;		
	}

	protected EthereumClientConfig getConfig(){
		return getEthereumService().getConfig();
	}

	protected IEthereumService getEthereumService() {
		return ICOFarmApplication.get().getInjector().getInstance(IEthereumService.class);
	}

	private Credentials getCredentials(String password, Wallet wallet) throws Exception {
		return getEthereumService().readWallet(password, wallet.getWalletJSON());
	}
}