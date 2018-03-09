package org.orienteer.widget;

import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.Collections;
import java.util.List;

@Widget(id = "registration", domain = "browse")
public class ICOFarmRegistrationWidget extends AbstractICOFarmWidget<OSecurityUser> {

    public ICOFarmRegistrationWidget(String id, IModel<OSecurityUser> model, IModel<ODocument> widgetDocumentModel) {
        super(id, model, widgetDocumentModel);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        Form form = new Form("form");
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
        add(form);
    }

    private SubmitLink newSubmitLink(String id) {
        return new SubmitLink(id) {
            @Override
            @SuppressWarnings("unchecked")
            public void onSubmit() {
                Form form = getForm();
                String firstName = ((TextField<String>) form.get("firstName")).getModelObject();
                String lastName = ((TextField<String>) form.get("lastName")).getModelObject();
                String email = ((TextField<String>) form.get("email")).getModelObject();
                String password = ((TextField<String>) form.get("password")).getModelObject();
                ODocument doc = createNewUser(email, password, firstName, lastName);
                DBClosure.sudoSave(doc);
                OrienteerWebSession.get().invalidate();
                OrienteerWebSession.get().authenticate(email, password);
            }

            private ODocument createNewUser(String email, String password, String firstName, String lastName) {
                ODocument doc = new ODocument(OUser.CLASS_NAME);
                ODocument role = getRoleForNewUser();
                ODocument perspective = role != null ? role.field("perspective") : null;
                doc.field("name", email);
                doc.field("password", password);
                doc.field("firstName", firstName);
                doc.field("lastName", lastName);
                doc.field("status", "ACTIVE");
                doc.field("perspective", perspective);
                doc.field("roles", role != null ? Collections.singletonList(role) : Collections.emptyList());
                return doc;
            }

            private ODocument getRoleForNewUser() {
                List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                        .query(new OSQLSynchQuery<>("select from " + ORole.CLASS_NAME + " where name = 'investor'", 1));
                return docs != null && !docs.isEmpty() ? docs.get(0) : null;
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
