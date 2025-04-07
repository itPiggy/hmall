package com.hmall.itemservice.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.itemservice.domain.po.Item;
import com.hmall.itemservice.domain.po.ItemDoc;
import com.hmall.itemservice.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-04
 * @Description:
 * @Version: 1.0
 */

@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticSearchDocTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

    @Test
    public void testDocProperties() throws IOException {
        // 获取商品信息
        Item item = itemService.getById(317578L);
        // 将Item对象转换为ItemDoc对象
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 创建对象
        IndexRequest indexRequest = new IndexRequest("items").id(String.valueOf(item.getId()));
        //IndexRequest indexRequest = new IndexRequest("items").id(itemDoc.getId());
        // 文档内容
        indexRequest.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        // 发送请求,操作文档不需要索引库(indices()),可直接链式操作文档
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testGetIndex() throws IOException {
        // 准备request
        GetRequest getRequest = new GetRequest("items", "317578");
        // 发送请求
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        // 返回的是一个文档整体，我们可以取出我们想要的资源
        String source = response.getSourceAsString();
        // 将json字符串转换为我们需要的实体对象
        ItemDoc itemDoc = JSONUtil.toBean(source, ItemDoc.class);
        System.out.println("itemDoc = " + itemDoc);
    }

    @Test
    public void testDeleteIndex() throws IOException {
        DeleteRequest request = new DeleteRequest("items", "317578");
        client.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * 这里是局部修改，全量修改的话可以参考testDocProperties()
     */
    @Test
    public void testUpdateIndex() throws IOException {
        UpdateRequest request = new UpdateRequest("items", "317578");
        Map<String, Object> sourceProperty = new HashMap<>();
        sourceProperty.put("price", 32100);
        request.doc(sourceProperty);
        client.update(request, RequestOptions.DEFAULT);
    }

    /**
     * 批量新增文档
     */
    @Test
    public void testBulkIndex() throws IOException {
        int pageNO = 1, pageSize = 1000;
        while(true) {
            // 准备文档资料
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNO, pageSize));
            List<Item> itemList = page.getRecords();
            if (CollUtil.isEmpty(itemList)) {
                return;
            }

            // 准备请求对象
            BulkRequest bulkRequest = new BulkRequest();
            // 请求内容
            itemList.forEach(item -> {
                bulkRequest.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)), XContentType.JSON));
            });
            // 发送请求
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
            // 翻页
            pageNO++;
        }
    }

    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.66.3:9200")
        ));
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
