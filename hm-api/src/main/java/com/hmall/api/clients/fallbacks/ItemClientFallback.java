package com.hmall.api.clients.fallbacks;

import com.hmall.api.clients.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.List;

@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {
    @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.error("查询商品失败！", cause);
                return CollUtils.emptyList(); // 返回空集合
            }

            @Override
            public void deductStock(List<OrderDetailDTO> orderDetailDTOS) {
                throw new RuntimeException(cause); // 这里直接抛出异常，让调用方处理
            }

            @Override
            public ItemDTO queryItemById(Long id) {
                return null;
            }
        };
    }
}
