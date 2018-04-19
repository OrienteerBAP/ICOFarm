package org.orienteer;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.exception.OSecurityAccessException;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.core.OrienteerWebSession;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.model.OMail;
import org.orienteer.service.IDBService;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(OrienteerTestRunner.class)
public class ICOFarmDbServiceTest {
    @Inject
    private IDBService dbService;

    private ICOFarmUser user;

    @Before
    public void init() {
        user = new ICOFarmUser(OrienteerWebSession.get().getEffectiveUser().getDocument());
    }

    @Test
    public void testReaderQueryUsers() {
        List<ODocument> docs = OrienteerWebSession.get().getDatabase().query(new OSQLSynchQuery<>("select from " + ICOFarmUser.CLASS_NAME));
        assertNotNull(docs);
        assertTrue(docs.size() == 1);
        assertTrue(docs.get(0).equals(user.getDocument()));
    }

    @Test
    public void testAdminQueryUsers() {
        List<ICOFarmUser> users = dbService.getUsers();
        assertTrue(users.size() > 1);
        assertTrue(users.contains(user));
    }

    @Test
    public void testQueryUsersById() {
        ICOFarmUser icoFarmUser = dbService.getUserBy(ICOFarmUser.OPROPERTY_ID, user.getId());
        assertEquals(icoFarmUser, user);
    }

    @Test(expected = OSecurityAccessException.class)
    public void testReaderQueryEmail() {
        OrienteerWebSession.get().getDatabase().query(new OSQLSynchQuery<>("select from " + OMail.CLASS_NAME));
    }

}
