package com.dating.apps.datingapps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatingappsApplication {

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");

        SpringApplication.run(DatingappsApplication.class, args);
    }

}