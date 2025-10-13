package com.eventhub.MailService.service;

import com.eventhub.MailService.dao.RemoveDTO;
import com.eventhub.MailService.model.Mail;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendMessage(Mail mail);
    void reminder(String message);
    void removeNotification(RemoveDTO removeDTO);
    void sendHTMLEmail(Mail mail) throws MessagingException;
    void sendEmailWithAttachment(Mail mail) throws MessagingException;
}
