package com.hmall.searchservice.constants;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-06
 * @Description:
 * @Version: 1.0
 */

public interface MQConstant {
    String SEARCH_DIRECT_NAME = "search.direct";
    String INDEX_QUEUE_NAME = "insert.queue";
    String INDEX_QUEUE_KEY = "insert";
    String DELETE_QUEUE_NAME = "delete.queue";
    String DELETE_QUEUE_KEY = "delete";
    String UPDATE_QUEUE_NAME = "update.queue";
    String UPDATE_QUEUE_KEY = "update";
}
