package com.hmall.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-03
 * @Description: MQ自动配置类
 * @Version: 1.0
 */

@Configuration
@ConditionalOnProperty(value = "spring.rabbitmq.listener.simple.retry.enabled", havingValue = "true")
public class MqConsumeErrorAutoConfiguration {

    @Value("${spring.application.name}")
    private String SERVER_NAME_QUEUE;

    @Bean
    public DirectExchange errorExchange() {
        return new DirectExchange("error.direct");
    }

    @Bean
    public Queue serverNameQueue(){
        return new Queue(SERVER_NAME_QUEUE + "error.queue");
    }

    @Bean
    public Binding serverNameBinding(Queue serverNameQueue, DirectExchange errorExchange){
        return BindingBuilder.bind(serverNameQueue).to(errorExchange).with(SERVER_NAME_QUEUE);
    }

    public MessageRecoverer republishMessageRecoverer(RabbitTemplate rabbitTemplate){
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", SERVER_NAME_QUEUE);
    }
}
