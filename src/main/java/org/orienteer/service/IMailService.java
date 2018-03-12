package org.orienteer.service;

import com.google.inject.ImplementedBy;

import javax.mail.MessagingException;

@ImplementedBy(MailServiceImpl.class)
public interface IMailService {
    public void sendMail(String to, String subject, String text, String type) throws MessagingException;
    public void sendMailAsync(String to, String subject, String text, String type);
    public void sendMail(String to, String subject, String text) throws MessagingException;
    public void sendMailAsync(String to, String subject, String text);
}
