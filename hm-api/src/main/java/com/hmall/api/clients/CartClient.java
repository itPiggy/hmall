package com.hmall.api.clients;

import com.hmall.api.config.DefaultOpenFeignLoggerConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;

@FeignClient(value = "cart-service", configuration = DefaultOpenFeignLoggerConfig.class)
public interface CartClient {

    @DeleteMapping("/carts")
    void removeByItemsIds(@RequestParam("ids") Collection<Long> ids);
}
