package com.hmall.gateway.filter;

import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class PrintAnyGatewayFilterFactory extends AbstractGatewayFilterFactory<PrintAnyGatewayFilterFactory.Config> {
    // 自定义过滤器
    /*@Override
    public GatewayFilter apply(Object config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                System.out.println("Request Headers: " + exchange.getRequest().getHeaders());
                return chain.filter(exchange);
            }
        }, 1);
    }*/


    // 自定义参数过滤器配置
    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                /*System.out.println("config.getA() = " + config.getA());
                System.out.println("config.getB() = " + config.getB());
                System.out.println("config.getC() = " + config.getC());
                System.out.println("Request Headers: " + exchange.getRequest().getHeaders());*/
                return chain.filter(exchange);
            }
        }, 1);
    }

    // 自定义配置属性，下面会用到
    @Data
    public static class Config {
        private String a;
        private String b;
        private String c;
    }

    // 返回顺寻，顺序很重要，是和Config中的以及yaml中的参数位置一致
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("a", "b", "c");
    }

    // 将Config字节码文件传给父类，父类负责帮我们读取yaml文件
    public PrintAnyGatewayFilterFactory() {
        super(Config.class);
    }
}