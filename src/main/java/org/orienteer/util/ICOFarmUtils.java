package org.orienteer.util;

import com.orientechnologies.orient.core.metadata.function.OFunction;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ICOFarmUtils {

    /**
     * Search user by email in database
     * @param email {@link String} user email
     * @return {@link ICOFarmUser} which represents user with given email or null if user with given email don't exists
     * in database
     */
    public static ICOFarmUser getUserByEmail(String email) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where "
                        + ICOFarmUser.EMAIL + " = ?", 1), email);
        return getFromDocs(docs, ICOFarmUser::new);
    }

    public static OFunction getOFunctionByName(String name) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase().query(
                new OSQLSynchQuery<>("select from " + OFunction.CLASS_NAME + " where name = ?", 1), name);
        return getFromDocs(docs, OFunction::new);
    }

    public static ICOFarmUser getUserBy(String field, String value) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>("select from " + OUser.CLASS_NAME + " where "
                        + field + " = ?", 1), value);
        return getFromDocs(docs, ICOFarmUser::new);
    }

    public static OMail getOMailByName(String name) {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase()
                .query(new OSQLSynchQuery<>(String.format("select from %s where %s = ?", OMail.CLASS_NAME, OMail.NAME),
                        1), name);
        return getFromDocs(docs, OMail::new);
    }

    public static Map<Object, Object> getUserMacros(ICOFarmUser user) {
        return getUserMacros(user.getDocument());
    }

    public static Map<Object, Object> getUserMacros(ODocument doc) {
        Map<Object, Object> map = new HashMap<>(1);
        map.put("firstName", doc.field(ICOFarmUser.FIRST_NAME));
        map.put("lastName", doc.field(ICOFarmUser.LAST_NAME));
        map.put("email", doc.field(ICOFarmUser.EMAIL));
        return map;
    }

    private static <T> T getFromDocs(List<ODocument> docs, Function<ODocument, T> f) {
        return docs != null && !docs.isEmpty() ? f.apply(docs.get(0)) : null;
    }
}
