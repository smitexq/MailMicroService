package com.eventhub.MailService.controller;

import com.eventhub.MailService.model.Mail;
import com.eventhub.MailService.service.MailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail-service")
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public void sendMessage(@RequestBody Mail mail) {
        mailService.sendMessage(mail);
    }
}
