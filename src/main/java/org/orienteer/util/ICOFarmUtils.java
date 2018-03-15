package org.orienteer.util;

import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.OrienteerWebSession;

import java.util.List;

public final class ICOFarmUtils {

    /**
     * Search user by email in database
     * @param email {@link String} user email
     * @return {@link ODocument} which represents user with given email or null if user with given email don't exists
     * in database
     */
    public static final ODocument getUserByEmail(String email) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where email = ?", 1), email);
        return docs != null && !docs.isEmpty() ? docs.get(0) : null;
    }
}
