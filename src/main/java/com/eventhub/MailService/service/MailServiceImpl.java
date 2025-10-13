package com.eventhub.MailService.service;

import com.eventhub.MailService.dao.InMemoryReminder;
import com.eventhub.MailService.dao.RemoveDTO;
import com.eventhub.MailService.dto.NotificationDTO;
import com.eventhub.MailService.model.Mail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String sender;

    private final List<UUID> toRemove = new ArrayList<>(); //список на отправку письма и удаление из мапы

    private final JavaMailSender mailSender;
    private final InMemoryReminder inMemoryReminder;
    private final ObjectMapper mapper;

    public MailServiceImpl(JavaMailSender mailSender, InMemoryReminder inMemoryReminder, ObjectMapper mapper) {
        this.mailSender = mailSender;
        this.inMemoryReminder = inMemoryReminder;
        this.mapper = mapper;
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
    public void sendHTMLEmail(Mail mail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        message.setFrom(new InternetAddress(sender));
        for (String recipient: mail.getRecipients()){
            message.addRecipients(MimeMessage.RecipientType.TO, recipient);
        }
        message.setSubject(mail.getSubject());
        message.setContent(mail.getBody(), "text/html; charset=utf-8");

        mailSender.send(message);
    }

    @Override
    public void sendEmailWithAttachment(Mail mail) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(sender);
        helper.setTo(mail.getRecipients());
        helper.setSubject("Testing Mail API With Attachment " + mail.getSubject());
        helper.setText("Please find the attached document below " + mail.getBody());

        ClassPathResource pathResource = new ClassPathResource("test.png");
        helper.addAttachment(Objects.requireNonNull(pathResource.getFilename()), pathResource);

        mailSender.send(message);
    }


    @KafkaListener(
            topics = "notification-topic",
            groupId = "reminder"
    )
    @Override
    public void reminder(String message) {
        try {
            NotificationDTO notificationDTO = mapper.readValue(message, NotificationDTO.class);
            inMemoryReminder.addRecipient(notificationDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeNotification(RemoveDTO removeDTO) {
        inMemoryReminder.removeRecipient(
                removeDTO.getUsername(),
                removeDTO.getEvent_name()
                );
    }



    @Scheduled(fixedDelay = 3*1000) //Проверка того, что пользователю нужно отправить уведомление
    private void test() {
        Map<UUID, NotificationDTO> recipients = inMemoryReminder.getRecipients();
        toRemove.clear(); //Очистка списка с ключами

        System.out.println(recipients.toString());
        for (UUID key : recipients.keySet()) {
            LocalDateTime res_time = recipients.get(key).getTime();

            //Для начала соберем ключи подходящих пользователей (чтобы не удалять из Map во время итерации
            if (res_time.isBefore(LocalDateTime.now().plusMinutes(5))
                    && res_time.isAfter(LocalDateTime.now())) {

                toRemove.add(key);
            }
        }

        toRemove.forEach( //Теперь удаляем каждого пользователя из списка (кому нужно отправить уведомление) и отправляем письмо
                key -> {
                    SimpleMailMessage smsg = new SimpleMailMessage();

                    NotificationDTO recipient = recipients.get(key);

                    smsg.setTo(recipient.getEmail());
                    smsg.setSubject("Не забудь о мероприятии!");
                    smsg.setText(String.format("Событие \"%s\" стартует %s в %s",
                            recipient.getEvent_name(),
                            recipient.getTime().toString().substring(0,10),
                            recipient.getTime().toString().substring(11,16)
                    ));
                    smsg.setFrom(sender);

                    mailSender.send(smsg);

                    //Удаляем элемент из списка
                    inMemoryReminder.removeRecipient(key);
                }
        );
    }
}
