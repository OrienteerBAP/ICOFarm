package org.orienteer.method;

import org.apache.wicket.model.IModel;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.model.EthereumWallet;
import org.orienteer.model.TokenCurrency;

import com.orientechnologies.orient.core.record.impl.ODocument;

@OMethod(
		icon=FAIconType.dollar,
		filters={
			@OFilter(fClass = ODocumentFilter.class, fData = "EthereumWallet"),
			@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)

public class BuyTokenFromWallet extends BuyToken{
	private static final long serialVersionUID = 1L;

	protected EthereumWallet getWallet() throws Exception{
		IModel<?> walletModel = getEnvData().getDisplayObjectModel();
		ODocument walletDoc = (ODocument) walletModel.getObject();
		if (walletDoc==null) throw new Exception("Please link buy button to 'EthereumWallet' OClass");
		return new EthereumWallet(walletDoc);
		
	}
	
	protected TokenCurrency getTokenCurrency() throws Exception{
		throw new Exception("Make 'default token currency' feature first");
		/*
		ICOFarmApplication.get().getModuleByName(name)
		OSecurityUser user = OrienteerWebSession.get().getUser();
		
		if (user==null)	throw new Exception("Please autorize");
		ICOFarmUser icofarmUser = new ICOFarmUser(user.getDocument());
		
		EthereumWallet wallet = icofarmUser.getMainETHWallet();
		if (wallet==null) throw new Exception("Please link correct ETC wallet to your account");
		return wallet;
		*/
	}
	
}
