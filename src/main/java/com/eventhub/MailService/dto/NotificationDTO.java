package com.eventhub.MailService.dto;

import java.time.LocalDateTime;

public class NotificationDTO {
    private String username;
    private String email;
    private String event_name;
    private LocalDateTime time;


    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getEvent_name() {
        return event_name;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
