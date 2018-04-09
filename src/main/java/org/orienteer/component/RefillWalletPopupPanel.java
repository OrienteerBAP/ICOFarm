package org.orienteer.component;

import com.orientechnologies.orient.core.record.impl.ODocument;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;
import org.apache.tika.Tika;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.ByteArrayResource;
import org.orienteer.model.EmbeddedWallet;
import org.orienteer.widget.AbstractICOFarmWidget;
import org.orienteer.widget.ICOFarmReferralsWidget;
import org.web3j.utils.Strings;

import java.io.ByteArrayOutputStream;

public class RefillWalletPopupPanel extends GenericPanel<ODocument> {
    public RefillWalletPopupPanel(String id, IModel<ODocument> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(new Label("title") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.setDefaultModelObject(new ResourceModel(!Strings.isEmpty(getAddress()) ?"refill.wallet.title" :
                        "refill.wallet.empty.title").getObject());
            }
        });

        add(new TextField<String>("address", Model.of()) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (Strings.isEmpty(getModel().getObject())) {
                    String address = getAddress();
                    if (!Strings.isEmpty(address)) {
                        setVisible(true);
                        setModelObject(address);
                    } else setVisible(false);
                }
            }
        }.setOutputMarkupId(true));

        add(new WebMarkupContainer("copyContainer") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!Strings.isEmpty(getAddress()));
            }
        }.setOutputMarkupId(true));

        add(createQrCode("qrCode"));
    }

    private Image createQrCode(String id) {
        return new Image(id) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                String address = getAddress();
                if (!Strings.isEmpty(address)) {
                    setVisible(true);
                    ByteArrayOutputStream qrCode = QRCode.from(address).to(ImageType.PNG).withSize(250, 250).stream();
                    byte[] bytes = qrCode.toByteArray();
                    setImageResource(new ByteArrayResource(new Tika().detect(bytes), bytes));
                } else setVisible(false);
            }
        };
    }

    private String getAddress() {
        return getModel().getObject().field(EmbeddedWallet.OPROPERTY_ADDRESS);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(AbstractICOFarmWidget.ICOFARM_WIDGET_CSS));
        response.render(JavaScriptHeaderItem.forReference(ICOFarmReferralsWidget.COPY_JS));
        response.render(OnDomReadyHeaderItem.forScript(String.format("addAutoCopyOnElement('%s', '%s');",
                get("address").getMarkupId(), get("copyContainer").getMarkupId()))
        );
    }
}
