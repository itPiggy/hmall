package com.hmall.api.config;

import cn.hutool.core.util.StrUtil;
import com.hmall.api.clients.fallbacks.ItemClientFallback;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

public class DefaultOpenFeignLoggerConfig {

    @Bean
    public Logger.Level defaultOpenFeignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Object user = UserContext.getUser();
                if (user == null) {
                    return;
                }
                String userId = user.toString();
                if (StrUtil.isEmpty(userId)) {
                    return;
                }
                requestTemplate.header("user-info", userId);
            }
        };
    }

    @Bean
    public ItemClientFallback itemClientFallbackFactory(){
        return new ItemClientFallback();
    }
}
