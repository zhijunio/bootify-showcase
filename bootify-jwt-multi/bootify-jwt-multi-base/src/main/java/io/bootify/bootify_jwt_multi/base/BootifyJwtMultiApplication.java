package io.bootify.bootify_jwt_multi.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan("io.bootify.bootify_jwt_multi")
public class BootifyJwtMultiApplication {

    public static void main(final String[] args) {
        SpringApplication.run(BootifyJwtMultiApplication.class, args);
    }

}
