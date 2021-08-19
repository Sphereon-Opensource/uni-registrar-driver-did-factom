package com.sphereon.uniregistrar.driver.did.factom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.sphereon.uniregistrar", "org.factomprotocol.identity.did"})
public class FactomDriverApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactomDriverApplication.class, args);
    }
}
