package com.gerasimchuk.droneslogistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DronesLogisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DronesLogisticsApplication.class, args);
    }


}
