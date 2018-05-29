package org.orienteer.method;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.component.InfoMessagePanel;
import org.orienteer.component.token.BuyTokenPanel;
import org.orienteer.core.component.command.AbstractModalWindowCommand;
import org.orienteer.core.method.methods.AbstractModalOMethod;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;

public abstract class AbstractBuyTokenMethod extends AbstractModalOMethod {

    @Override
    public Component getModalContent(String componentId, ModalWindow modal, AbstractModalWindowCommand<?> command) {
        modal.setMinimalWidth(370);
        modal.setAutoSize(true);
        Token token = getToken();
        if (token != null) {
            if (token.getAddress() == null) {
                command.setVisible(false);
            } else command.setVisible(!token.isEthereumCurrency());
        }

        return new BuyTokenPanel(componentId, Model.of(getWallet()), Model.of(token)) {

            @Override
            public void onBuyTokens(AjaxRequestTarget target) {
                modal.setContent(new InfoMessagePanel(modal.getContentId(), new ResourceModel("buy.token.success.text")) {
                    @Override
                    protected void onOkClick(AjaxRequestTarget target) {
                        modal.close(target);
                    }
                });
                modal.setTitle(new ResourceModel("info.title"));
                modal.close(target);
                modal.show(target);
                command.onAfterModalSubmit();
            }
        };
    }

    protected abstract Wallet getWallet();
    protected abstract Token getToken();
}
