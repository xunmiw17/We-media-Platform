package com.xunmiw.article.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * 声明构建Channel
 */
@Component
public interface StreamChannel {

    String INPUT = "mqInput";
    String OUTPUT = "mqOutput";

    @Output(StreamChannel.OUTPUT)
    MessageChannel output();

    @Input(StreamChannel.INPUT)
    MessageChannel input();
}
