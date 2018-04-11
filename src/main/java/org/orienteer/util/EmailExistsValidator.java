package org.orienteer.util;

import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.service.IDBService;

import java.util.HashMap;
import java.util.Map;

public class EmailExistsValidator implements IValidator<String> {

    private final boolean exists;

    public EmailExistsValidator(boolean exists) {
        this.exists = exists;
    }

    @Override
    public void validate(IValidatable<String> validatable) {
        String email = validatable.getValue();
        if (exists != isUserExists(email)) {
            Map<String, String> map = new HashMap<>(1);
            map.put("email", email);
            ValidationError error = new ValidationError();
            error.setMessage(new StringResourceModel(getResource(), new MapModel<>(map)).getString());
            validatable.error(error);
        }
    }

    private boolean isUserExists(String email) {
        return OrienteerWebApplication.get().getServiceInstance(IDBService.class).getUserBy(ICOFarmUser.EMAIL, email) != null;
    }

    private String getResource() {
        return exists ? "email.validator.notExists" : "email.validator.exists";
    }
}
