package com.gachiganjik.gachiganjik_server.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    @Profile("dev")
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender devMailSender() {
        return new JavaMailSenderImpl(); // 실제 전송 안 함, 빈만 등록
    }
}