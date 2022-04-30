package com.xunmiw.api.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ARTICLE_EXCHANGE = "article_exchange";

    public static final String ARTICLE_DOWNLOAD_QUEUE = "article_download_queue";

    // 指定Bean的名字为交换机名字
    @Bean(ARTICLE_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(ARTICLE_EXCHANGE)
                                .durable(true)
                                .build();
    }

    // 指定Bean的名字为队列名字
    @Bean(ARTICLE_DOWNLOAD_QUEUE)
    public Queue queue() {
        return new Queue(ARTICLE_DOWNLOAD_QUEUE);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue())
                                .to(exchange())
                                .with("article.#")
                                .noargs();        // 执行绑定
    }
}
