package com.hmall.tradeservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmall.tradeservice.domain.dto.OrderFormDTO;
import com.hmall.tradeservice.domain.po.Order;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);
}
