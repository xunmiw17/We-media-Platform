package com.xunmiw.article.consumer;

import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.article.service.ArticleService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    @Autowired
    private ArticleService articleService;

    @RabbitListener(queues = {RabbitMQConfig.ARTICLE_DELAYED_PUBLISH_QUEUE})
    public void listen(String payload, Message message) {
        String articleId = payload;
        articleService.updateArticleAppointStatus(articleId);
    }
}
