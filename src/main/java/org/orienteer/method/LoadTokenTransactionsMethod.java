package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.orienteer.component.transaction.LoadTokenTransactionsPanel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.filters.PlaceFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.Token;

@OMethod(titleKey = "method.token.load.transactions",
//        filters = {
//            @OFilter(fClass = ODocumentFilter.class, fData = Token.CLASS_NAME),
//            @OFilter(fClass = WidgetTypeFilter.class, fData = ICOFarmPerspectiveModule.TOKEN_TRANSACTIONS_WIDGET_ID),
//            @OFilter(fClass = PlaceFilter.class, fData = "DATA_TABLE")
//        },
         filters = {
				@OFilter(fClass = ODocumentFilter.class, fData = Token.CLASS_NAME),
				@OFilter(fClass = PlaceFilter.class, fData = "STRUCTURE_TABLE"),
		},
        bootstrap = BootstrapType.PRIMARY,
        icon = FAIconType.plus
)
public class LoadTokenTransactionsMethod extends AbstractModalOMethod {

    @Override
    @SuppressWarnings("unchecked")
    public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
        modal.setAutoSize(true);

        IModel<ODocument> model = (IModel<ODocument>) getContext().getDisplayObjectModel();
        Token token = new Token(model.getObject());

        return new LoadTokenTransactionsPanel(componentId, Model.of(token)) {
            @Override
            protected void onTransactionsLoaded(AjaxRequestTarget target, Token token) {
                modal.close(target);
            }
        };
    }
}
