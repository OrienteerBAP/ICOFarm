package org.orienteer;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.security.OUser;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.model.ICOFarmUser;
import org.orienteer.service.IRestorePasswordService;

@RunWith(OrienteerTestRunner.class)
public class RestorePasswordServiceTest {

    @Inject
    private IRestorePasswordService service;

    private ICOFarmUser user;

    @Before
    public void init() {
        user = new ICOFarmUser(new ODocument(OUser.CLASS_NAME));
        user.setEmail("test@gmail.com");
    }

    @Test
    public void  testService() {

    }
}
