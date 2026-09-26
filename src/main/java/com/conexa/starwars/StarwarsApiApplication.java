package com.conexa.starwars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StarwarsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarwarsApiApplication.class, args);
    }

}
