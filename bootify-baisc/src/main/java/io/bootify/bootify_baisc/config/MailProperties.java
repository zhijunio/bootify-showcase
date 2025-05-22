package io.bootify.bootify_baisc.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Getter
public class MailProperties {

    @Value("${app.mail.from}")
    private String mailFrom;

}
