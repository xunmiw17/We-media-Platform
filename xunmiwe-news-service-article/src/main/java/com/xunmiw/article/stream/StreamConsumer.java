package com.xunmiw.article.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * 构建消费端监听并实现消息的消费
 */
@Component
@EnableBinding(StreamChannel.class)
public class StreamConsumer {

    // @StreamListener(StreamChannel.INPUT)
    // public void consume(AppUser user) {
    //     System.out.println(user.toString());
    // }

    @StreamListener(StreamChannel.INPUT)
    public void consume(String dumpling) {
        System.out.println(dumpling);
    }
}
