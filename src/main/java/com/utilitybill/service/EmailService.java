package com.utilitybill.service;

import com.utilitybill.util.AppLogger;
import com.utilitybill.exception.NotificationException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class EmailService implements NotificationService {

    private static final String CLASS_NAME = EmailService.class.getName();
    private static volatile EmailService instance;

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "465";
    private static final String USERNAME = "alakeinioluwa21@gmail.com";
    private static final String PASSWORD = "fmcswkaufkjjkivq";


    private EmailService() {}

    public static EmailService getInstance() {
        if (instance == null) {
            synchronized (EmailService.class) {
                if (instance == null) {
                    instance = new EmailService();
                }
            }
        }
        return instance;
    }

    @Override

    public void sendNotification(String to, String subject, String body, File attachment) throws NotificationException {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", SMTP_HOST);
            prop.put("mail.smtp.port", SMTP_PORT);

            if ("smtp.example.com".equals(SMTP_HOST)) {
                AppLogger.info(CLASS_NAME, "SIMULATION: Sending email to " + to + " with subject '" + subject + "'");
                AppLogger.info(CLASS_NAME, "Attachment: " + (attachment != null ? attachment.getName() : "None"));
                return;
            }

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // specific parts
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Attachment
            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                try {
                    attachmentPart.attachFile(attachment);
                } catch (IOException e) {
                   throw new MessagingException("Failed to attach file: " + attachment.getName(), e);
                }
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);

            Transport.send(message);
            AppLogger.info(CLASS_NAME, "Email sent successfully to " + to);

        } catch (MessagingException e) {
            AppLogger.error(CLASS_NAME, "Failed to send email", e);
            throw new NotificationException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
