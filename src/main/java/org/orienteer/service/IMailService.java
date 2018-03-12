package org.orienteer.service;

import com.google.inject.ImplementedBy;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

@ImplementedBy(MailServiceImpl.class)
public interface IMailService {
    public void sendMail(String to, String subject, String text, String type) throws MessagingException, UnsupportedEncodingException;
    public void sendMailAsync(String to, String subject, String text, String type);
    public void sendMail(String to, String subject, String text) throws MessagingException, UnsupportedEncodingException;
    public void sendMailAsync(String to, String subject, String text);
}
