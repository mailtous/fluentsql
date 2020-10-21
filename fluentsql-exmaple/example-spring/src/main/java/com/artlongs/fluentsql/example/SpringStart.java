package com.artlongs.fluentsql.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.artlongs"})
public class SpringStart {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(SpringStart.class, args);
    }

 }