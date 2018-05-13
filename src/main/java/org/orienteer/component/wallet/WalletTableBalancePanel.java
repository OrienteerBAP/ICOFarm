package org.orienteer.component.wallet;

import com.orientechnologies.orient.core.record.impl.ODocument;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.model.Token;
import org.orienteer.model.Wallet;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WalletTableBalancePanel extends GenericPanel<Wallet> {

    public WalletTableBalancePanel(String id, IModel<Wallet> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        GenericTablePanel<ODocument> panel = new GenericTablePanel<>("balanceTable", createColumns(), createDataProvider(), 20);
        add(panel);
    }

    private List<IColumn<ODocument, String>> createColumns() {
        List<IColumn<ODocument, String>> columns = new ArrayList<>(2);

        columns.add(new AbstractColumn<ODocument, String>(new ResourceModel("wallet.table.balance.token.name")) {
            @Override
            public void populateItem(Item<ICellPopulator<ODocument>> item, String id, IModel<ODocument> rowModel) {
                item.add(new Label(id, getTokenName(rowModel.getObject())));
            }
        });

        columns.add(new AbstractColumn<ODocument, String>(new ResourceModel("wallet.table.balance.token.symbol")) {
            @Override
            public void populateItem(Item<ICellPopulator<ODocument>> item, String id, IModel<ODocument> rowModel) {
                String symbol = rowModel.getObject().field(Token.OPROPERTY_SYMBOL);
                item.add(new Label(id, Model.of(symbol)));
            }
        });

        columns.add(new AbstractColumn<ODocument, String>(new ResourceModel("wallet.table.balance.token.value")) {
            @Override
            public void populateItem(Item<ICellPopulator<ODocument>> item, String id, IModel<ODocument> rowModel) {
                Wallet wallet = WalletTableBalancePanel.this.getModelObject();
                Token token = new Token(rowModel.getObject());
                item.add(new Label(id, Model.of(wallet.getBalance(token.getSymbol()))));
            }
        });

        return columns;
    }

    private String getTokenName(ODocument doc) {
        Map<String, String> names = doc.field(Token.OPROPERTY_NAME);
        String name = names.get(OrienteerWebSession.get().getLocale().toLanguageTag());
        return name != null ? name : names.get(Locale.ENGLISH.toLanguageTag());
    }

    private OQueryDataProvider<ODocument> createDataProvider() {
        return new OQueryDataProvider<>("select from " + Token.CLASS_NAME);
    }
}
