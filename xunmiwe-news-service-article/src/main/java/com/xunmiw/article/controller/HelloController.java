package com.xunmiw.article.controller;

import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.grace.result.GraceJSONResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("producer")
public class HelloController{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("hello")
    public Object hello() {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, "article.frank", "Xunmiw's消息");
        return GraceJSONResult.ok();
    }
}
