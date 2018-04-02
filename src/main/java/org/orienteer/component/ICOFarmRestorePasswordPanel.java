package org.orienteer.component;

import com.google.inject.Inject;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.resource.ICOFarmRestorePasswordResource;
import org.orienteer.service.IICOFarmDbService;
import org.orienteer.service.IRestorePasswordService;
import org.orienteer.util.EmailExistsValidator;
import org.orienteer.web.ICOFarmLoginPage;

import java.util.function.BiConsumer;

public class ICOFarmRestorePasswordPanel extends AbstractICOFarmLoginPanel {

    @Inject
    private IRestorePasswordService service;

    @Inject
    private IICOFarmDbService dbService;

    public ICOFarmRestorePasswordPanel(String id) {
        super(id);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        FeedbackPanel panel = new FeedbackPanel("feedback");
        configFeedbackPanel(panel);
        Form form = createForm("form");
        form.add(new Button("submit", new ResourceModel("restore.button.submit")));
        add(panel);
        add(form);
        AttributeModifier.append("class", "signin-panel");
    }

    private Form createForm(String id) {
        return new Form<Object>(id) {

            @Override
            protected void onInitialize() {
                super.onInitialize();
                TextField<String> password = createPasswordField("password", new ResourceModel("password"));
                TextField<String> repeatPass = createPasswordField("repeatPassword", new ResourceModel("password.reenter"));
                add(new EqualPasswordInputValidator(password, repeatPass));
                add(createMailField("email"));
                add(password);
                add(repeatPass);
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void onSubmit() {
                ICOFarmUser user = getUser();
                if (user != null) {
                    String password = ((TextField<String>) get("password")).getModelObject();
                    user.setPassword(password);
                    user.sudoSave();
                    service.clearRestoring(user);
                    PageParameters params = new PageParameters();
                    params.add("restore", "success");
                    setResponsePage(ICOFarmLoginPage.class, params);
                } else {
                    TextField<String> field = ((TextField<String>) get("email"));
                    String email = field.getModelObject();
                    user = dbService.getUserBy(ICOFarmUser.EMAIL, email);
                    service.restoreUserPassword(user);
                    success(new ResourceModel("restore.check.email").getObject());
                }
            }

            private TextField<String> createMailField(String id) {
                TextField<String> field = new RequiredTextField<>(id, Model.of());
                field.add(EmailAddressValidator.getInstance());
                field.add(new EmailExistsValidator(true));
                configInputField(field, new ResourceModel("login.email.placeholder").getObject());
                return field;
            }

            private TextField<String> createPasswordField(String id, IModel<String> model) {
                TextField<String> field = new PasswordTextField(id, Model.of());
                field.setRequired(true);
                configInputField(field, model.getObject());
                return field;
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                BiConsumer<Component, Boolean> f = (c, b) -> c.setVisible(b).setEnabled(b);
                boolean newPassword = getUser() != null;
                f.accept(get("email"), !newPassword);
                f.accept(get("password"), newPassword);
                f.accept(get("repeatPassword"), newPassword);
                if (newPassword) success(new ResourceModel("password.restore").getObject());
            }

            private ICOFarmUser getUser() {
                String id = getWebPage().getPageParameters().get(ICOFarmRestorePasswordResource.RES_KEY).toString();
                return dbService.getUserBy(ICOFarmUser.RESTORE_ID, id);
            }
        };
    }
}
