package com.utilitybill.service;

import java.io.File;

import com.utilitybill.exception.NotificationException;

public interface NotificationService {
    void sendNotification(String to, String subject, String body, File attachment) throws NotificationException;
}
