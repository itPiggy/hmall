package com.hmall.cartservice.listener;

import com.hmall.cartservice.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CartClearListener {

    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic", type = ExchangeTypes.TOPIC),
            key = "order.create"
    ))
    /*public void listenClearCart(List<Long> ids) {
        cartService.removeByItemIds(ids);
    }*/
    public void listenClearCart(Map<String, Object> messageDate) {
        Object userId = messageDate.get("userId");
        UserContext.setUser(Long.valueOf(userId.toString()));
        Object itemIds = messageDate.get("itemIds");
        Collection<Long> ids = (Collection<Long>) itemIds;
        cartService.removeByItemIds(ids);
    }
}
