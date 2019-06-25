package com.artlongs.fluentsql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@ComponentScan({"com.artlongs"})
public class AppStart {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(AppStart.class, args);
//        CountDownLatch closeLatch = context.getBean(CountDownLatch.class);
//        closeLatch.await();
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

}