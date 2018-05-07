package org.orienteer.component.transaction;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.orienteer.model.Token;

public class TokenTransactionsPanel extends GenericPanel<Token> {
    public TokenTransactionsPanel(String id, IModel<Token> model) {
        super(id, model);
    }
}
