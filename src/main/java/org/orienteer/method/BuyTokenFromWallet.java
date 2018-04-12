package org.orienteer.method;

import org.apache.wicket.model.IModel;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.model.EthereumClientConfig;
import org.orienteer.model.EthereumWallet;
import org.orienteer.model.TokenCurrency;
import org.orienteer.service.IEthereumService;

import com.google.inject.Inject;
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

	@Override
	protected EthereumWallet getWallet() throws Exception{
		IModel<?> walletModel = getEnvData().getDisplayObjectModel();
		ODocument walletDoc = (ODocument) walletModel.getObject();
		if (walletDoc==null) throw new Exception("Please link buy button to 'EthereumWallet' OClass");
		return new EthereumWallet(walletDoc);
		
	}
	
	@Override
	protected TokenCurrency getTokenCurrency() throws Exception{
		TokenCurrency tokenCurrency = getConfig().getMainTokenCurrency(); 
		if (tokenCurrency.getDocument()==null) throw new Exception("Please set main token currency in ICOFarm module settings!");
		return tokenCurrency;		
	}
}
