package com.hmall.tradeservice.listener;

import com.hmall.api.clients.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.tradeservice.constants.MqConstant;
import com.hmall.tradeservice.domain.po.Order;
import com.hmall.tradeservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-03
 * @Description:
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConstant.DELAY_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = MqConstant.DELAY_DIRECT_NAME, delayed = "true"),
            key = MqConstant.DELAY_ROUTING_KEY
    ))
    public void listenOrderMessage(Long orderId){
        // 获取订单详情信息
        Order order = orderService.getById(orderId);
        // 判断订单是否已支付
        if (order == null || order.getStatus() != 1){
            return;
        }

        // 获取订单支付流水的详情
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        if (payOrderDTO != null && payOrderDTO.getStatus() == 3){
            // 若订单支付流水为已支付，则更新为已支付
            orderService.markOrderPaySuccess(orderId);
        }else {
            // 若订单支付流水为未支付，则取消订单，并恢复库存数量
            orderService.cancelOrder(orderId);
        }
    }
}
