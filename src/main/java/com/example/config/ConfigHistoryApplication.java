package com.example.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.config.mapper", "com.example.config.history.mapper"})
public class ConfigHistoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigHistoryApplication.class, args);
    }
}
