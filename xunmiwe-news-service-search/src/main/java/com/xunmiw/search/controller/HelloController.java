package com.xunmiw.search.controller;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.search.pojo.Stu;
import org.elasticsearch.action.index.IndexRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("createIndex")
    public Object createIndex() {
        elasticsearchTemplate.createIndex(Stu.class);
        return GraceJSONResult.ok("Hello");
    }

    @GetMapping("deleteIndex")
    public Object deleteIndex() {
        elasticsearchTemplate.deleteIndex(Stu.class);
        return GraceJSONResult.ok("Hello");
    }

    @GetMapping("addDoc")
    public Object addDoc() {
        Stu stu = new Stu();
        stu.setStuId(1001l);
        stu.setAge(10);
        stu.setDesc("this");
        stu.setMoney(100.2f);
        stu.setName("frank");

        IndexQuery query = new IndexQueryBuilder().withObject(stu).build();
        elasticsearchTemplate.index(query);
        return GraceJSONResult.ok("ok");
    }

    @GetMapping("updateDoc")
    public Object updateDoc() {

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("desc", "hello world");
        updateMap.put("age", 22);

        IndexRequest ir = new IndexRequest();
        ir.source(updateMap);

        UpdateQuery query = new UpdateQueryBuilder()
                .withClass(Stu.class)
                .withId("1001")
                .withIndexRequest(ir)
                .build();
        elasticsearchTemplate.update(query);

        return GraceJSONResult.ok("ok");
    }

    @GetMapping("getDoc")
    public Object getDoc(String id) {
        GetQuery query = new GetQuery();
        query.setId(id);

        Stu stu = elasticsearchTemplate.queryForObject(query, Stu.class);
        return GraceJSONResult.ok(stu);
    }

    @GetMapping("deleteDoc")
    public Object deleteDoc(String id) {
        elasticsearchTemplate.delete(Stu.class, id);
        return GraceJSONResult.ok();
    }
}
