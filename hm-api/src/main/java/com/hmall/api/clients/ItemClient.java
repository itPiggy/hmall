package com.hmall.api.clients;

import com.hmall.api.clients.fallbacks.ItemClientFallback;
import com.hmall.api.config.DefaultOpenFeignLoggerConfig;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "item-service", configuration = DefaultOpenFeignLoggerConfig.class
                , fallbackFactory = ItemClientFallback.class)
public interface ItemClient {

    @GetMapping("/items")
    List<ItemDTO> queryItemByIds(@RequestParam("ids") Collection<Long> ids);

    @PutMapping("/items/stock/deduct")
    void deductStock(@RequestBody List<OrderDetailDTO> orderDetailDTOS);

    @GetMapping("/items/{id}")
    ItemDTO queryItemById(@PathVariable("id") Long id);
}
