package com.utilitybill.service;

import com.utilitybill.util.AppLogger;
import com.utilitybill.exception.NotificationException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService implements NotificationService {

    private static final String CLASS_NAME = EmailService.class.getName();
    private static volatile EmailService instance;

    // Email configuration - loaded from config file or environment variables
    private String smtpHost;
    private String smtpPort;
    private String username;
    private String password;
    private boolean configured = false;

    private EmailService() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        // Try loading from config file first
        Properties config = new Properties();
        File configFile = new File("config/email.properties");

        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                config.load(input);
                smtpHost = config.getProperty("smtp.host", "smtp.gmail.com");
                smtpPort = config.getProperty("smtp.port", "465");
                username = config.getProperty("smtp.username", "");
                password = config.getProperty("smtp.password", "");
                configured = !username.isEmpty() && !password.isEmpty();
                AppLogger.info(CLASS_NAME, "Email configuration loaded from config/email.properties");
            } catch (IOException e) {
                AppLogger.warning("Failed to load email config: " + e.getMessage());
            }
        }

        // Fall back to environment variables if not configured
        if (!configured) {
            smtpHost = System.getenv("SMTP_HOST");
            smtpPort = System.getenv("SMTP_PORT");
            username = System.getenv("SMTP_USERNAME");
            password = System.getenv("SMTP_PASSWORD");

            if (smtpHost == null) smtpHost = "smtp.gmail.com";
            if (smtpPort == null) smtpPort = "465";

            configured = username != null && password != null && !username.isEmpty() && !password.isEmpty();

            if (configured) {
                AppLogger.info(CLASS_NAME, "Email configuration loaded from environment variables");
            } else {
                AppLogger.warning("Email not configured. Set SMTP_USERNAME and SMTP_PASSWORD environment variables or create config/email.properties");
            }
        }
    }

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

    public boolean isConfigured() {
        return configured;
    }

    @Override
    public void sendNotification(String to, String subject, String body, File attachment) throws NotificationException {
        if (!configured) {
            AppLogger.warning("Email not configured - simulating send to " + to);
            AppLogger.info(CLASS_NAME, "SIMULATION: Email to " + to + " with subject '" + subject + "'");
            AppLogger.info(CLASS_NAME, "Attachment: " + (attachment != null ? attachment.getName() : "None"));
            return;
        }

        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.host", smtpHost);
            prop.put("mail.smtp.port", smtpPort);

            Session session = Session.getInstance(prop, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

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
            AppLogger.info(CLASS_NAME, "Email sent successfully to " + to + " with subject: " + subject);

        } catch (MessagingException e) {
            com.utilitybill.util.AppLogger.error(CLASS_NAME, "Failed to send email", e);
            throw new NotificationException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
