package com.xunmiw.zuul.controller;

import com.xunmiw.api.controller.user.HelloControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController implements HelloControllerApi {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Override
    public Object hello() {
        return GraceJSONResult.ok();
    }
}
