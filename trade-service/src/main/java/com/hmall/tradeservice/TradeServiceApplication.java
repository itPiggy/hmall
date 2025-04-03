package com.hmall.tradeservice;

import com.hmall.api.config.DefaultOpenFeignLoggerConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.hmall.tradeservice.mapper")
@EnableFeignClients(basePackages = "com.hmall.api.clients", defaultConfiguration = DefaultOpenFeignLoggerConfig.class)
public class TradeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeServiceApplication.class, args);
    }

}