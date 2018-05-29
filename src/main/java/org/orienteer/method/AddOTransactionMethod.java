package org.orienteer.method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.orienteer.component.transaction.AddOTransactionPanel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.OClassBrowseFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.OTransaction;

@OMethod(titleKey = "method.transaction.add",
        filters = @OFilter(fClass = OClassBrowseFilter.class, fData = OTransaction.CLASS_NAME),
        bootstrap = BootstrapType.PRIMARY,
        icon = FAIconType.plus
)
public class AddOTransactionMethod extends AbstractModalOMethod {

    @Override
    public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
        modal.setAutoSize(true);
        modal.setMinimalHeight(250);
        modal.setMinimalWidth(350);

        modal.setWindowClosedCallback((t) -> t.add(getContext().getCurrentWidget()));

        return new AddOTransactionPanel(componentId) {
            @Override
            protected void onAddTransaction(OTransaction transaction, AjaxRequestTarget target) {
                modal.close(target);
            }
        };
    }
}
