package com.laiye.performance;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("com.laiye.performance.dao")
public class jacocoT {

    public static void main(String[] args) {
        SpringApplication.run(jacocoT.class, args);
    }

}