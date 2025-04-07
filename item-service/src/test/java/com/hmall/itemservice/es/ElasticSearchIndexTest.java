package com.hmall.itemservice.es;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * @Author: piggy
 * @CreateTime: 2025-04-04
 * @Description: 测试es的连接性
 * @Version: 1.0
 */

public class ElasticSearchIndexTest {

    private RestHighLevelClient client;

    @Test
    public void testRestApiConnect(){
        System.out.println(client);
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


    /*
    * 测试索引库的CURD
    * */
    @Test
    public void testCreateIndex() throws IOException {
        // 准备请求对象
        CreateIndexRequest items = new CreateIndexRequest("items");

        // 源：索引库的mapping映射，内容为JSON格式
        items.source(MAPPING_TEMPLATE, XContentType.JSON);

        // 发送请求
        client.indices().create(items, RequestOptions.DEFAULT);
    }

    @Test
    public void testGetIndex() throws IOException {
        // 创建请求对象
        GetIndexRequest items = new GetIndexRequest("items");

        // 发送请求,get请求返回一堆数据，不利于观察，判断是否存在即可
        //client.indices().get(items, RequestOptions.DEFAULT);
        boolean exists = client.indices().exists(items, RequestOptions.DEFAULT);
        System.out.println("exists = " + exists);
    }

    @Test
    public void testDeleteIndex() throws IOException {
        // 创建请求对象
        DeleteIndexRequest items = new DeleteIndexRequest("items");

        // 发送请求
        client.indices().delete(items, RequestOptions.DEFAULT);
    }



    private static final String MAPPING_TEMPLATE = "{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_smart\"\n" +
            "      },\n" +
            "      \"price\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\": {\n" +
            "        \"type\": \"keyword\", \n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\": {\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\": {\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\": {\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\": {\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
