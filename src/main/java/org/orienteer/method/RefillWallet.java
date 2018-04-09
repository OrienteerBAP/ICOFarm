package org.orienteer.method;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.orienteer.component.RefillWalletPopupPanel;
import org.orienteer.core.component.BootstrapType;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.OFilter;
import org.orienteer.core.method.OMethod;
import org.orienteer.core.method.filters.ODocumentFilter;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.EmbeddedWallet;

@OMethod(filters = {
        @OFilter(fClass = ODocumentFilter.class, fData = EmbeddedWallet.CLASS_NAME)
})
public class RefillWallet extends AbstractModalOMethod {

    @Override
    @SuppressWarnings("unchecked")
    public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
        IModel<ODocument> model = (IModel<ODocument>) getEnvData().getDisplayObjectModel();
        command.setIcon(FAIconType.dollar);
        command.setBootstrapType(BootstrapType.PRIMARY);
        modal.setMinimalWidth(580);
        modal.setMinimalHeight(370);
        return new RefillWalletPopupPanel(componentId, model);
    }
}
