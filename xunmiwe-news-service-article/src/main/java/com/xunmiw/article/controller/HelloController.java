package com.xunmiw.article.controller;

import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.grace.result.GraceJSONResult;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

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

    @GetMapping("delay")
    public Object delay() {

        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                // 设置消息的持久
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                // 设置消息延迟时间（单位ms）
                message.getMessageProperties().setDelay(5000);
                return message;
            }
        };

        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_DELAYED_PUBLISH_EXCHANGE, "delay.article", "延迟消息", messagePostProcessor);
        System.out.println("发送消息：" + new Date());
        return GraceJSONResult.ok();
    }
}
