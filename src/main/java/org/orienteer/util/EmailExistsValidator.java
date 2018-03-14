package org.orienteer.util;

import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.MapModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.orienteer.core.OrienteerWebSession;

import java.util.HashMap;
import java.util.List;
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
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where name = ?", 1), email);
        return docs != null && !docs.isEmpty();
    }

    private String getResource() {
        return exists ? "email.validator.notExists" : "email.validator.exists";
    }
}
