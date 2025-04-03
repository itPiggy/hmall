package com.hmall.tradeservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.clients.CartClient;
import com.hmall.api.clients.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.config.MqConsumeErrorAutoConfiguration;
import com.hmall.common.exception.BadRequestException;
import com.hmall.common.utils.UserContext;
import com.hmall.tradeservice.constants.MqConstant;
import com.hmall.tradeservice.domain.dto.OrderFormDTO;
import com.hmall.tradeservice.domain.po.Order;
import com.hmall.tradeservice.domain.po.OrderDetail;
import com.hmall.tradeservice.mapper.OrderMapper;
import com.hmall.tradeservice.service.IOrderDetailService;
import com.hmall.tradeservice.service.IOrderService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

    //private final IItemService itemService;
    private final IOrderDetailService detailService;
    //private final ICartService cartService;
    //private final CartClient cartClient;
    private final RabbitTemplate rabbitTemplate;
    private final ItemClient itemClient;

    @Override
    @GlobalTransactional
    public Long createOrder(OrderFormDTO orderFormDTO) {
        // 1.订单数据
        Order order = new Order();
        // 1.1.查询商品
        List<OrderDetailDTO> detailDTOS = orderFormDTO.getDetails();
        // 1.2.获取商品id和数量的Map
        Map<Long, Integer> itemNumMap = detailDTOS.stream()
                .collect(Collectors.toMap(OrderDetailDTO::getItemId, OrderDetailDTO::getNum));
        Set<Long> itemIds = itemNumMap.keySet();
        // 1.3.查询商品
        //List<ItemDTO> items = itemService.queryItemByIds(itemIds);
        List<ItemDTO> items = itemClient.queryItemByIds(itemIds);
        if (items == null || items.size() < itemIds.size()) {
            throw new BadRequestException("商品不存在");
        }
        // 1.4.基于商品价格、购买数量计算商品总价：totalFee
        int total = 0;
        for (ItemDTO item : items) {
            total += item.getPrice() * itemNumMap.get(item.getId());
        }
        order.setTotalFee(total);
        // 1.5.其它属性
        order.setPaymentType(orderFormDTO.getPaymentType());
        order.setUserId(UserContext.getUser());
        order.setStatus(1);
        // 1.6.将Order写入数据库order表中
        save(order);

        // 2.保存订单详情
        List<OrderDetail> details = buildDetails(order.getId(), items, itemNumMap);
        detailService.saveBatch(details);

        // 3.清理购物车商品
        //cartService.removeByItemIds(itemIds);
        //cartClient.removeByItemsIds(itemIds);
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("userId", UserContext.getUser());
            messageData.put("itemIds", itemIds);
            rabbitTemplate.convertAndSend("trade.topic", "order.create", messageData);
        }catch (Exception e){
            log.error("清理购物车失败，订单id{}", itemIds, e);
        }

        // 4.扣减库存
        try {
            itemClient.deductStock(detailDTOS);
        } catch (Exception e) {
            throw new RuntimeException("库存不足！");
        }

        rabbitTemplate.convertAndSend(MqConstant.DELAY_DIRECT_NAME,
                MqConstant.DELAY_ROUTING_KEY, order.getId(),
                message -> {
                    message.getMessageProperties().setDelay(10000);
                    return message;
                });
        return order.getId();
    }

    @Override
    public void markOrderPaySuccess(Long orderId) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(2);
        order.setPayTime(LocalDateTime.now());
        updateById(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        // 取消订单信息
        Order order = getById(orderId);
        order.setStatus(5);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);

        // 恢复库存
        List<OrderDetailDTO> orderDetailDTOS = new ArrayList<>();
        List<OrderDetail> orderDetails = detailService.lambdaQuery().eq(OrderDetail::getOrderId, orderId).list();
        orderDetails.forEach(orderDetail -> {
            OrderDetailDTO orderDetailDTO = new OrderDetailDTO()
                    .setNum(-orderDetail.getNum())
                    .setItemId(orderDetail.getItemId());
            orderDetailDTOS.add(orderDetailDTO);
        });
        try{
            itemClient.deductStock(orderDetailDTOS);
        }catch (Exception e){
            log.error("恢复库存失败，订单id{}", orderId, e);
        }
    }

    private List<OrderDetail> buildDetails(Long orderId, List<ItemDTO> items, Map<Long, Integer> numMap) {
        List<OrderDetail> details = new ArrayList<>(items.size());
        for (ItemDTO item : items) {
            OrderDetail detail = new OrderDetail();
            detail.setName(item.getName());
            detail.setSpec(item.getSpec());
            detail.setPrice(item.getPrice());
            detail.setNum(numMap.get(item.getId()));
            detail.setItemId(item.getId());
            detail.setImage(item.getImage());
            detail.setOrderId(orderId);
            details.add(detail);
        }
        return details;
    }
}
