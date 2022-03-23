package com.xunmiw.user.controller;

import com.xunmiw.api.controller.user.HelloControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController implements HelloControllerApi {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private RedisOperator redisOperator;

    public Object hello() {
        logger.debug("Debug: hello");
        logger.info("Info: hello");
        logger.warn("warn: hello");
        logger.error("error: hello");
        return GraceJSONResult.ok();
    }

    @GetMapping("/redis")
    public Object redis() {
        redisOperator.set("age", "18");
        return GraceJSONResult.ok(redisOperator.get("age"));
    }
}
