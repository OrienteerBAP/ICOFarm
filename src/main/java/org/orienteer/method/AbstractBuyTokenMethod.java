package org.orienteer.method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.orienteer.component.BuyTokenPanel;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.TokenCurrency;
import org.orienteer.model.Wallet;

public abstract class AbstractBuyTokenMethod extends AbstractModalOMethod {

    @Override
    public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
        modal.setMinimalWidth(370);
        modal.setAutoSize(true);

        return new BuyTokenPanel(componentId, Model.of(getWallet()), Model.of(getTokenCurrency())) {

            @Override
            public void onBuyTokens(AjaxRequestTarget target) {
                modal.close(target);
                command.onAfterModalSubmit();
            }
        };
    }

    protected abstract Wallet getWallet();
    protected abstract TokenCurrency getTokenCurrency();
}
