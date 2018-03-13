package org.orienteer.widget;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.resource.ICOFarmRegistrationResource;
import org.orienteer.service.IMailService;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

@Widget(id = "registration", domain = "browse")
public class ICOFarmRegistrationWidget extends AbstractICOFarmWidget<OSecurityUser> {

    @Inject
    private IMailService mailService;

    public ICOFarmRegistrationWidget(String id, IModel<OSecurityUser> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form form = createForm("form");
        TextField<String> emailTextField = new RequiredTextField<>("email", Model.<String>of());
        TextField<String> passwordTextField = new PasswordTextField("password", Model.<String>of());
        TextField<String> reEnterPassword = new PasswordTextField("repeatPassword", Model.<String>of());
        emailTextField.add(EmailAddressValidator.getInstance());
        emailTextField.add(new UserExistsValidator());
        form.add(new EqualPasswordInputValidator(passwordTextField, reEnterPassword));
        form.add(new RequiredTextField<>("firstName", Model.<String>of()));
        form.add(new RequiredTextField<>("lastName", Model.<String>of()));
        form.add(emailTextField);
        form.add(passwordTextField);
        form.add(reEnterPassword);
        form.add(newSubmitLink("submit"));
        form.add(new Label("title", new ResourceModel("widget.registration.title")));
        form.add(new Label("content", new ResourceModel("widget.registration.content")));
        add(form);
        add(createSuccessPanel("feedback"));
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id) {
            @Override
            @SuppressWarnings("unchecked")
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String firstName = ((TextField<String>) form.get("firstName")).getModelObject();
                String lastName = ((TextField<String>) form.get("lastName")).getModelObject();
                String email = ((TextField<String>) form.get("email")).getModelObject();
                String password = ((TextField<String>) form.get("password")).getModelObject();
                ODocument doc = createNewUser(email, password, firstName, lastName);
                DBClosure.sudoSave(doc);
                sendActivationEmail(doc);
                target.add(ICOFarmRegistrationWidget.this);
            }

            private ODocument createNewUser(String email, String password, String firstName, String lastName) {
                ODocument doc = new ODocument(OUser.CLASS_NAME);
                ODocument role = getRoleForNewUser();
                ODocument perspective = role != null ? role.field("perspective") : null;
                doc.field("name", email);
                doc.field("email", email);
                doc.field("password", password);
                doc.field("firstName", firstName);
                doc.field("lastName", lastName);
                doc.field("status", OUser.STATUSES.SUSPENDED);
                doc.field("perspective", perspective);
                doc.field("id", UUID.randomUUID().toString());
                doc.field("roles", role != null ? Collections.singletonList(role) : Collections.emptyList());
                return doc;
            }

            private ODocument getRoleForNewUser() {
                List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                        .query(new OSQLSynchQuery<>("select from " + ORole.CLASS_NAME + " where name = 'investor'", 1));
                return docs != null && !docs.isEmpty() ? docs.get(0) : null;
            }

            private void sendActivationEmail(ODocument doc) {
                IModel<Map<String, String>> macros = createMacrosMap(doc);
                String email = doc.field("email");
                String subject = new StringResourceModel("widget.registration.mail.subject", macros).getObject();
                String text = new StringResourceModel("widget.registration.mail.text", macros).getObject();
                mailService.sendMailAsync(email, subject, text, "registration");
            }

            private IModel<Map<String, String>> createMacrosMap(ODocument doc) {
                Map<String, String> map = new HashMap<>(1);
                map.put("firstName", doc.field("firstName"));
                map.put("lastName", doc.field("lastName"));
                map.put("link", ICOFarmRegistrationResource.genRegistrationLink(doc));
                return new MapModel<>(map);
            }
        };
    }

    private Form createForm(String id) {
        return new Form(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
            }

            @Override
            protected void onSubmit() {
                super.onSubmit();
                setVisible(false);
                ICOFarmRegistrationWidget.this.get("feedback").setVisible(true);
            }
        };
    }

    private WebMarkupContainer createSuccessPanel(String id) {
        return new WebMarkupContainer(id) {
            @Override
            protected void onInitialize() {
                super.onInitialize();
                setOutputMarkupPlaceholderTag(true);
                add(new Label("title", new ResourceModel("widget.registration.success.title")));
                add(new Label("content", new ResourceModel("widget.registration.success.content")));
                setVisible(false);
            }
        };
    }

    @Override
    protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.user);
    }

    @Override
    protected IModel<String> getDefaultTitleModel() {
        return Model.of("Registration");
    }

    private static class UserExistsValidator implements IValidator<String> {

        @Override
        public void validate(IValidatable<String> validatable) {
            String email = validatable.getValue();
            List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                    .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where name = ?", 1), email);
            if (docs != null && !docs.isEmpty()) {
                ValidationError error = new ValidationError(this);
                error.setVariable("email", email);
                validatable.error(error);
            }
        }
    }
}
