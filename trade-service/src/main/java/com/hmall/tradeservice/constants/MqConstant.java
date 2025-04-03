package com.hmall.tradeservice.constants;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-03
 * @Description: 延迟消息常量
 * @Version: 1.0
 */

public interface MqConstant {

    String DELAY_DIRECT_NAME = "pay.delay.order.direct";

    String DELAY_QUEUE_NAME = "pay.delay.queue ";

    String DELAY_ROUTING_KEY = "order.delay.order";
}
