package org.orienteer.widget;

import com.google.common.base.Strings;
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
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.widget.Widget;
import org.orienteer.model.OMail;
import org.orienteer.resource.ICOFarmRegistrationResource;
import org.orienteer.service.IOMailService;
import org.orienteer.util.EmailExistsValidator;
import org.orienteer.util.ICOFarmUtils;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import java.util.*;

import static org.orienteer.ICOFarmModule.*;

@Widget(id = "registration", domain = "browse")
public class ICOFarmRegistrationWidget extends AbstractICOFarmWidget<OSecurityUser> {

    @Inject
    private IOMailService mailService;

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
        emailTextField.add(new EmailExistsValidator(false));
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
                ODocument referral = createReferral(doc);
                DBClosure.sudoSave(doc);
                if (referral != null) DBClosure.sudoSave(referral);
                sendActivationEmail(doc);
                target.add(ICOFarmRegistrationWidget.this);
            }

            private ODocument createNewUser(String email, String password, String firstName, String lastName) {
                ODocument doc = new ODocument(OUser.CLASS_NAME);
                ODocument role = getRoleForNewUser();
//                ODocument perspective = role != null ? new ODocument((ORID) role.field("perspective")) : null;
                doc.field("name", email);
                doc.field("email", email);
                doc.field("password", password);
                doc.field("firstName", firstName);
                doc.field("lastName", lastName);
                doc.field("status", OUser.STATUSES.SUSPENDED);
//                doc.field("perspective", perspective);
                doc.field("id", UUID.randomUUID().toString());
                doc.field("roles", role != null ? Collections.singletonList(role) : Collections.emptyList());
                return doc;
            }

            private ODocument createReferral(ODocument user) {
                String id = (String) OrienteerWebSession.get().getAttribute("referral");
                ODocument by = !Strings.isNullOrEmpty(id) ? getUserById(id) : null;
                ODocument doc = null;
                if (by != null) {
                    doc = new ODocument(REFERRAL);
                    doc.field(OPROPERTY_REFERRAL_CREATED, new Date());
                    doc.field(OPROPERTY_REFERRAL_USER, user);
                    doc.field(OPROPERTY_REFERRAL_BY, by);
                }
                return doc;
            }

            private ODocument getRoleForNewUser() {
                List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                        .query(new OSQLSynchQuery<>("select from " + ORole.CLASS_NAME + " where name = 'investor'", 1));
                return docs != null && !docs.isEmpty() ? docs.get(0) : null;
            }

            private void sendActivationEmail(ODocument doc) {
                Map<Object, Object> macros = ICOFarmUtils.getUserMacros(doc);
                String email = doc.field("email");
                OMail oMail = ICOFarmUtils.getOMailByName("registration");
                macros.put("link", ICOFarmRegistrationResource.genRegistrationLink(doc));
                oMail.setMacros(macros);
                mailService.sendMailAsync(email, oMail);
            }

            private ODocument getUserById(String id) {
                List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                        .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where id = ?", 1), id);
                return docs != null && !docs.isEmpty() ? docs.get(0) : null;
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

}
