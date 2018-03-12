package org.orienteer;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.orienteer.core.service.Orienteer;
import org.orienteer.junit.OrienteerTestRunner;
import org.orienteer.service.IMailService;
import ru.ydn.wicket.wicketorientdb.utils.DBClosure;

import javax.mail.MessagingException;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.orienteer.ICOFarmModule.*;

@RunWith(OrienteerTestRunner.class)
public class MailServiceTest {

    @Inject
    private IMailService mailService;

    @Inject
    @Orienteer
    private Properties properties;

    private ODocument doc;
    private String to;

    @Before
    public void init() {
        to = properties.getProperty("icofarm.email.to");
        doc = new DBClosure<ODocument>() {
            @Override
            protected ODocument execute(ODatabaseDocument db) {
                ODocument doc = new ODocument(MAIL_CONFIG);
                doc.field(OPROPERTY_MAIL_CONFIG_SMTP_HOST, "smtp.gmail.com");
                doc.field(OPROPERTY_MAIL_CONFIG_SMTP_PORT, 587);
                doc.field(OPROPERTY_MAIL_CONFIG_TYPE, getClass().getName());
                doc.field(OPROPERTY_MAIL_CONFIG_EMAIL, properties.getProperty("icofarm.email"));
                doc.field(OPROPERTY_MAIL_CONFIG_PASSWORD, properties.getProperty("icofarm.password"));
                doc.save();
                return doc;
            }
        }.execute();
    }

    @After
    public void destroy() {
        new DBClosure<Void>() {
            @Override
            protected Void execute(ODatabaseDocument db) {
                db.command(new OCommandSQL("delete from ?")).execute(doc);
                return null;
            }
        }.execute();
    }

    @Test
    public void sendEmail() {
        boolean success = false;
        try {
            mailService.sendMail(to, "Test", "<h1>Test ICOFarm mail service</h1>", doc.field(OPROPERTY_MAIL_CONFIG_TYPE));
            success = true;
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        assertTrue(success);
    }

    @Test
    public void throwError() {
        try {
            mailService.sendMail(to, "Test", "<h1>Test ICOFarm mail service</h1>", UUID.randomUUID().toString());
        } catch (Exception ex) {
            assertTrue(ex instanceof IllegalStateException);
        }
    }
}
