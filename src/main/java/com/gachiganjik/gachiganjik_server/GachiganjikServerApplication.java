package com.gachiganjik.gachiganjik_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class GachiganjikServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GachiganjikServerApplication.class, args);
    }

}
