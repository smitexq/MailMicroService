package com.eventhub.MailService.service;

import com.eventhub.MailService.dao.InMemoryReminder;
import com.eventhub.MailService.dao.RemoveDTO;
import com.eventhub.MailService.dto.NotificationDTO;
import com.eventhub.MailService.model.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MailServiceImpl implements MailService {

    @Value("${spring.mail.username}")
    private String sender;

    List<UUID> toRemove = new ArrayList<>(); //список на отправку письма и удаление из мапы

    private final JavaMailSender mailSender;
    private final InMemoryReminder inMemoryReminder;

    public MailServiceImpl(JavaMailSender mailSender, InMemoryReminder inMemoryReminder) {
        this.mailSender = mailSender;
        this.inMemoryReminder = inMemoryReminder;
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
        inMemoryReminder.addRecipient(notificationDTO);
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
