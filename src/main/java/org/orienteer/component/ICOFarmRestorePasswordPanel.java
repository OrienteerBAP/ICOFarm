package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.service.IRestorePasswordService;
import org.orienteer.util.EmailExistsValidator;
import org.orienteer.util.ICOFarmUtils;

public class ICOFarmRestorePasswordPanel extends AbstractICOFarmLoginPanel {

    @Inject
    private IRestorePasswordService service;

    public ICOFarmRestorePasswordPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        FeedbackPanel panel = new FeedbackPanel("feedback");
        configFeedbackPanel(panel);
        Form form = createForm("form");
        form.add(createMailField("email"));
        form.add(new Button("submit", new ResourceModel("restore.button.submit")));
        add(panel);
        add(form);
        AttributeModifier.append("class", "signin-panel");
    }

    private Form createForm(String id) {
        return new Form(id) {
            @Override
            @SuppressWarnings("unchecked")
            protected void onSubmit() {
                String email = ((TextField<String>) get("email")).getModelObject();
                ICOFarmUser user = ICOFarmUtils.getUserByEmail(email);
                service.restoreUserPassword(user);
            }
        };
    }

    private TextField<String> createMailField(String id) {
        TextField<String> field = new RequiredTextField<>(id, Model.of());
        field.add(EmailAddressValidator.getInstance());
        field.add(new EmailExistsValidator(true));
        configInputField(field, new ResourceModel("login.email.placeholder").getObject());
        return field;
    }
}
