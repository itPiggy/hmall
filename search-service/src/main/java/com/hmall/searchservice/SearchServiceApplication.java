package com.hmall.searchservice;

import com.hmall.api.config.DefaultOpenFeignLoggerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan("com.hmall.searchservice.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.clients", defaultConfiguration = DefaultOpenFeignLoggerConfig.class)
@SpringBootApplication
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }

}
