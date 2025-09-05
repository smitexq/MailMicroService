package com.eventhub.MailService.service;

import com.eventhub.MailService.model.Mail;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendMessage(Mail mail) {
        System.out.println("письмо");
        SimpleMailMessage smsg = new SimpleMailMessage();

        smsg.setTo(mail.getRecipients());
        smsg.setSubject(mail.getSubject());
        smsg.setText(mail.getBody());

        mailSender.send(smsg);
    }
}
