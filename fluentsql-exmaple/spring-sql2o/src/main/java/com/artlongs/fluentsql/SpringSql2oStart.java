package com.artlongs.fluentsql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.sql2o.Sql2o;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@ComponentScan({"com.artlongs"})
public class SpringSql2oStart {

    @Autowired
    Environment environment;

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(SpringSql2oStart.class, args);
//        CountDownLatch closeLatch = context.getBean(CountDownLatch.class);
//        closeLatch.await();
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    @Bean
    public Sql2o buildSql2o() {
        String proPfix = "spring.datasource.";
        String url = environment.getProperty(proPfix + "url");
        String username = environment.getProperty(proPfix + "username");
        String password = environment.getProperty(proPfix + "password");
        String dcn = environment.getProperty(proPfix + "driver-class-name");
        int maxPoolSize = getInt(environment.getProperty(proPfix + "hikari.maximum-pool-size"),10);
        int minIdle = getInt(environment.getProperty(proPfix + "hikari.minimum-idle"), 5);
        Sql2o sql2o = Sql2OBuilder.buildOfHikariCP(url,username,password,dcn,maxPoolSize,minIdle);
        return sql2o;
    }

    private int getInt(Object s, int def) {
        if(null == s) return def;
        return Integer.valueOf(""+s);
    }

}