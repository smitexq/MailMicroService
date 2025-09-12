package com.eventhub.MailService.dao;

import com.eventhub.MailService.dto.NotificationDTO;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class InMemoryReminder {
    private final Map<UUID, NotificationDTO> recipients = new HashMap();

    public void addRecipient(NotificationDTO recipient) {
        UUID id = UUID.nameUUIDFromBytes(
                (recipient.getEvent_name() + recipient.getUsername()).getBytes()
        );

        recipients.put(id,recipient);
    }

    public Map<UUID, NotificationDTO> getRecipients() {
        return this.recipients;
    }


    public void removeRecipient(UUID key) {
        recipients.remove(key);
    }
    public void removeRecipient(String username, String event_name) {
        UUID key = UUID.nameUUIDFromBytes(
                (event_name + username).getBytes()
        );

        recipients.remove(key);
    }



}
