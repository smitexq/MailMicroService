package com.eventhub.MailService.service;

import com.eventhub.MailService.dto.NotificationDTO;
import com.eventhub.MailService.model.Mail;

public interface MailService {
    void sendMessage(Mail mail);
    void reminder(NotificationDTO notificationDTO);
}
