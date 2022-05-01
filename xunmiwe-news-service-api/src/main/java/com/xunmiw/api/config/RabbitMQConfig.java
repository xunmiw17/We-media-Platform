package com.xunmiw.api.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ARTICLE_EXCHANGE = "article_exchange";
    public static final String ARTICLE_DELAYED_PUBLISH_EXCHANGE = "article_delayed_publish_exchange";

    public static final String ARTICLE_DOWNLOAD_QUEUE = "article_download_queue";
    public static final String ARTICLE_DELAYED_PUBLISH_QUEUE = "article_delayed_publish_queue";

    // 指定Bean的名字为交换机名字
    @Bean(ARTICLE_EXCHANGE)
    public Exchange exchange() {
        return ExchangeBuilder.topicExchange(ARTICLE_EXCHANGE)
                .durable(true)
                .build();
    }

    // 延迟队列交换机
    @Bean(ARTICLE_DELAYED_PUBLISH_EXCHANGE)
    public Exchange delayedExchange() {
        return ExchangeBuilder.topicExchange(ARTICLE_DELAYED_PUBLISH_EXCHANGE)
                .delayed()              // 开启支持延迟消息
                .durable(true)
                .build();
    }

    // 指定Bean的名字为队列名字
    @Bean(ARTICLE_DOWNLOAD_QUEUE)
    public Queue queue() {
        return new Queue(ARTICLE_DOWNLOAD_QUEUE);
    }

    // 延迟队列
    @Bean(ARTICLE_DELAYED_PUBLISH_QUEUE)
    public Queue delayedQueue() {
        return new Queue(ARTICLE_DELAYED_PUBLISH_QUEUE);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue())
                .to(exchange())
                .with("article.#")
                .noargs();        // 执行绑定
    }

    // 延迟队列绑定
    @Bean
    public Binding delayedQueueBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayedExchange())
                .with("delay.#")
                .noargs();
    }
}
