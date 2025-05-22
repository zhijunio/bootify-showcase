package io.bootify.bootify_form_thymeleaf.service;

import io.bootify.bootify_form_thymeleaf.config.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    public MailService(final JavaMailSender javaMailSender, final MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailProperties = mailProperties;
    }

    @Async
    public void sendMail(final String mailTo, final String subject, final String html) {
        log.info("sending mail {} to {}", subject, mailTo);

        javaMailSender.send(mimeMessage -> {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setFrom(mailProperties.getMailFrom());
            message.setTo(mailTo);
            message.setSubject(subject);
            message.setText(html, true);
        });

        log.info("sending completed");
    }

}
