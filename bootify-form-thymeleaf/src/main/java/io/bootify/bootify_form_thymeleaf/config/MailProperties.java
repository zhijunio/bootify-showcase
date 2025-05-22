package io.bootify.bootify_form_thymeleaf.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MailProperties {

    @Value("${app.mail.from}")
    private String mailFrom;

    public String getMailFrom() {
        return mailFrom;
    }

}
