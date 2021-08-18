package com.sphereon.uniregistrar.driver.did.factom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.oas.annotations.EnableOpenApi;

@SpringBootApplication(scanBasePackages = { "com.sphereon.uniregistrar", "org.factomprotocol.identity.did"})
public class FactomDriverApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactomDriverApplication.class, args);
    }
}
