package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.orienteer.ICOFarmApplication;
import org.orienteer.component.BuyTokenPanel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;
import org.orienteer.service.web3.IEthereumService;
import org.web3j.crypto.Credentials;

@OMethod(
		icon = FAIconType.dollar,
		filters = {
				@OFilter(fClass = ODocumentFilter.class, fData = "TokenCurrency"),
				@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)
public class BuyToken extends AbstractModalOMethod {
	private static final long serialVersionUID = 1L;

	
	@Override
	public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
		modal.setMinimalWidth(370);
		modal.setAutoSize(true);

		return new BuyTokenPanel(componentId, getWallet(), getTokenCurrency()) {

			@Override
			public void onBuyTokens(AjaxRequestTarget target) {
				modal.close(target);
				command.onAfterModalSubmit();
			}
		};
	}
	
	protected Wallet getWallet() {
		ICOFarmUser icofarmUser = new ICOFarmUser(OrienteerWebSession.get().getUser().getDocument());
		Wallet wallet = icofarmUser.getMainETHWallet();
		if (wallet == null) {
			throw new IllegalStateException("Please link correct ETC wallet to your account");
		}
		return wallet;
	}
	
	protected TokenCurrency getTokenCurrency() {
		IModel<?> currencyModel = getContext().getDisplayObjectModel();
		ODocument currencyDoc = (ODocument) currencyModel.getObject();
		if (currencyDoc == null) {
			throw new IllegalStateException("Please link buy button to 'currency' OClass");
		}
		return new TokenCurrency(currencyDoc);		
	}

	protected EthereumClientConfig getConfig() {
		return getEthereumService().getConfig();
	}

	protected IEthereumService getEthereumService() {
		return ICOFarmApplication.get().getInjector().getInstance(IEthereumService.class);
	}

	private Credentials getCredentials(String password, Wallet wallet) throws Exception {
		return getEthereumService().readWallet(password, wallet.getWalletJSON());
	}
}

