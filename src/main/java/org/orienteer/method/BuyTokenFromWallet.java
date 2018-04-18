package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;

@OMethod(
		icon = FAIconType.dollar,
		filters = {
			@OFilter(fClass = ODocumentFilter.class, fData = "Wallet"),
			@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)
public class BuyTokenFromWallet extends BuyToken{
	private static final long serialVersionUID = 1L;

	@Override
	protected Wallet getWallet() {
		IModel<?> walletModel = getContext().getDisplayObjectModel();
		return new Wallet((ODocument) walletModel.getObject());
	}
	
	@Override
	protected TokenCurrency getTokenCurrency() {
		TokenCurrency tokenCurrency = getConfig().getMainTokenCurrency(); 
		if (tokenCurrency == null) {
			throw new IllegalStateException("Please set main token currency in ICOFarm module settings!");
		}
		return tokenCurrency;		
	}
}
