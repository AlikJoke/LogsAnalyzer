package org.analyzer.service.scheduled;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.analyzer.entities.NotificationSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataIndexingMailNotifier implements DataIndexingNotifier {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${logs.analyzer.notifications.mail.from}")
    private String emailFrom;

    @Override
    public void notifySuccess(@NonNull String indexingKey, @NonNull String successMessage, @NonNull NotificationSettings notificationSettings) {
        this.send("Logs indexing completed (%s)".formatted(indexingKey), successMessage, notificationSettings);
    }

    @Override
    public void notifyError(@NonNull String errorMessage, @NonNull NotificationSettings notificationSettings) {
        this.send("Logs indexing failed with error", errorMessage, notificationSettings);
    }

    private void send(final String subject, final String bodyText, final NotificationSettings settings) {

        final SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setSubject(subject);
        mailMessage.setFrom(this.emailFrom);
        mailMessage.setTo(settings.getNotifyToEmail());
        mailMessage.setText(bodyText);

        this.mailSender.send(mailMessage);
    }
}
