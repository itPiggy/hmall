package com.hmall.gateway.routes;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicRouterLoader {

    private final NacosConfigManager nacosConfigManager;

    private final RouteDefinitionWriter writer;

    private final String dataId = "gateway-routes.json";

    private final String group = "DEFAULT_GROUP";

    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouterConfig() throws NacosException {
        // 项目启动时先拉取一次配置，并配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 4000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        // 定义线程池
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        // 当配置发生变更时，推送更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        // 第一次读取到配置，也需要更新到路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo) {
        // 解析配置信息，将json格式转为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);

        // 删除旧的路由表信息
        for (String routeId : routeIds) {
            writer.delete(Mono.just(routeId)).subscribe();
        }

        // 更新路由表信息
        for (RouteDefinition routeDefinition : routeDefinitions) {
            // 更新路由表信息
            writer.save(Mono.just(routeDefinition)).subscribe();

            // 记录id，便于下一次更新时删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
