package com.emtdev.tus.spring;

import com.emtdev.tus.spring.server.TusNettyConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@Import(TusNettyConfiguration.class)

public class TusNettySpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TusNettySpringApplication.class, args);
    }

}
