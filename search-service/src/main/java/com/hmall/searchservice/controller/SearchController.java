package com.hmall.searchservice.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.api.dto.ItemDTO;
import com.hmall.common.domain.PageDTO;
import com.hmall.searchservice.domain.po.Item;
import com.hmall.searchservice.domain.po.ItemDoc;
import com.hmall.searchservice.domain.query.ItemPageQuery;
import com.hmall.searchservice.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final RestHighLevelClient restHighLevelClient = new RestHighLevelClient(RestClient.builder(
            HttpHost.create("http://192.168.66.3:9200")
    ));

    @ApiOperation("根据id查找商品")
    @GetMapping("/{id}")
    public ItemDTO findById(@PathVariable Long id) throws IOException {
        // 准备请求对象
        GetRequest item = new GetRequest("items").id(id.toString());
        // 查找数据
        GetResponse response = restHighLevelClient.get(item, RequestOptions.DEFAULT);
        // 解析数据
        String source = response.getSourceAsString();
        // 反序列化数据，并返回
        return BeanUtil.copyProperties(JSONUtil.toBean(source, ItemDoc.class), ItemDTO.class);
    }
}
