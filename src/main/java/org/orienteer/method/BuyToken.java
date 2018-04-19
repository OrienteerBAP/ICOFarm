package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.model.IModel;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;

@OMethod(
		icon = FAIconType.dollar,
		filters = {
				@OFilter(fClass = ODocumentFilter.class, fData = Token.CLASS_NAME),
				@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		}
)
public class BuyToken extends AbstractBuyTokenMethod {

	@Override
	protected Wallet getWallet() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Token getToken() {
		IModel<ODocument> docModel = (IModel<ODocument>) getContext().getDisplayObjectModel();
		return new Token(docModel.getObject());
	}
}

