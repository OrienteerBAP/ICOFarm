package org.orienteer.component.token;

import com.google.inject.Inject;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.Wallet;
import org.orienteer.service.IDBService;
import org.orienteer.util.ComponentUtils;

import java.util.List;

public class ChooseWalletAddressPanel extends FormComponentPanel<String> {

    @Inject
    private IDBService dbService;

    private DropDownChoice<Wallet> walletSelect;
    private TextField<String> addressInput;

    private boolean selectWallet = true;

    public ChooseWalletAddressPanel(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    public void convertInput() {
        if (selectWallet) {
            Wallet wallet = walletSelect.getConvertedInput();
            setConvertedInput(wallet != null ? wallet.getAddress() : null);
        } else setConvertedInput(addressInput.getConvertedInput());

        setModelObject(getConvertedInput());
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(newSelectWalletLink("selectWalletLink"));
        add(newInputAddressLink("inputAddressLink"));
        add(walletSelect = createSelectWalletDropdown("selectWallet"));
        add(addressInput = createInputAddressField("inputAddress"));
        setOutputMarkupPlaceholderTag(true);
    }

    private DropDownChoice<Wallet> createSelectWalletDropdown(String id) {
        List<Wallet> userWallets = dbService.getUserWallets(OrienteerWebSession.get().getUserAsODocument());
        DropDownChoice<Wallet> select = new DropDownChoice<>(id, Model.of(), userWallets,
                ComponentUtils.getChoiceRendererForWallets());
        return select;
    }

    private TextField<String> createInputAddressField(String id) {
        TextField<String> field = new TextField<>(id, Model.of());
        return field;
    }

    private AjaxLink<Void> newSelectWalletLink(String id) {
        return new AjaxLink<Void>(id) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(new ResourceModel("choose.link.icofarm.wallet"));
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("href", "#icofarm-wallet");
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                selectWallet = true;
            }
        };
    }

    private AjaxLink<Void> newInputAddressLink(String id) {
        return new AjaxLink<Void>(id) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                setBody(new ResourceModel("choose.link.custom.wallet"));
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("href", "#custom-wallet");
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                selectWallet = false;
            }
        };
    }
}
