package org.orienteer.service;

import com.google.common.base.Strings;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.orienteer.core.OrienteerWebApplication;
import org.orienteer.core.OrienteerWebSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import static org.orienteer.ICOFarmModule.*;

public class MailServiceImpl implements IMailService {

    private static final Logger LOG = LoggerFactory.getLogger(MailServiceImpl.class);

    @Override
    public void sendMail(String to, String subject, String text, String type) throws MessagingException, UnsupportedEncodingException {
        ODocument doc = searchEmailSettings(type);
        Message message = new MimeMessage(createSession(doc));
        InternetAddress from = new InternetAddress(doc.field(OPROPERTY_MAIL_CONFIG_EMAIL), doc.field(OPROPERTY_MAIL_CONFIG_FROM));
        message.setFrom(from);
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(text, "text/html");
        Transport.send(message);
    }

    @Override
    public void sendMail(String to, String subject, String text) throws MessagingException, UnsupportedEncodingException {
        sendMail(to, subject, text, null);
    }

    @Override
    public void sendMailAsync(String to, String subject, String text, String type) {
        OrienteerWebApplication app = OrienteerWebApplication.get();
        RequestCycle cycle = RequestCycle.get();
        OrienteerWebSession session = OrienteerWebSession.get();
        new Thread(() -> {
            try {
                ThreadContext.setApplication(app);
                ThreadContext.setRequestCycle(cycle);
                ThreadContext.setSession(session);
                sendMail(to, subject, text, type);
            } catch (MessagingException | UnsupportedEncodingException ex) {
                LOG.error("Can't send message to: {}", to, ex);
            }
        }).start();
    }

    @Override
    public void sendMailAsync(String to, String subject, String text) {
        sendMailAsync(to, subject, text, null);
    }


    private Session createSession(ODocument doc) {
        Properties properties = getEmailProperties(doc);
        Authenticator authenticator = createAuthenticatior(doc.field(OPROPERTY_MAIL_CONFIG_EMAIL), doc.field(OPROPERTY_MAIL_CONFIG_PASSWORD));
        return Session.getInstance(properties, authenticator);
    }

    private Properties getEmailProperties(ODocument doc) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", doc.field(OPROPERTY_MAIL_CONFIG_SMTP_HOST));
        properties.put("mail.smtp.port", doc.field(OPROPERTY_MAIL_CONFIG_SMTP_PORT));
        return properties;
    }

    private Authenticator createAuthenticatior(String username, String password) {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };
    }

    private ODocument searchEmailSettings(String type) {
        if (Strings.isNullOrEmpty(type)) type = "default";
        String sql = String.format("select from %s where %s = ?", MAIL_CONFIG, OPROPERTY_MAIL_CONFIG_TYPE);
        List<ODocument> docs = OrienteerWebSession.get().getDatabase().query(new OSQLSynchQuery<>(sql, 1), type);
        if (docs != null && !docs.isEmpty()) {
            return docs.get(0);
        } else throw new IllegalStateException("No mail config for email type: '" + type + "'");
    }
}
