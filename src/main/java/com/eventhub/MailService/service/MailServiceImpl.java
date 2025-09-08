package com.eventhub.MailService.service;

import com.eventhub.MailService.dto.NotificationDTO;
import com.eventhub.MailService.model.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final JavaMailSender mailSender;

    public MailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendMessage(Mail mail) {
        SimpleMailMessage smsg = new SimpleMailMessage();

        smsg.setTo(mail.getRecipients());
        smsg.setSubject(mail.getSubject());
        smsg.setText(mail.getBody());
        smsg.setFrom(sender);

        mailSender.send(smsg);
    }

    @Override
    public void reminder(NotificationDTO notificationDTO) {
        System.out.println(notificationDTO.getEmail() + " " + notificationDTO.getUsername() + " " + notificationDTO.getTime());

    }


    @Scheduled(fixedDelay = 3000) //Проверка того, что пользователю нужно отправить уведомление
    public void test() {
//        System.out.println("{}");
    }
}
