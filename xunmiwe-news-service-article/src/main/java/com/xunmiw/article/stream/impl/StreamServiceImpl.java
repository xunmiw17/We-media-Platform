package com.xunmiw.article.stream.impl;

import com.netflix.discovery.converters.Auto;
import com.xunmiw.article.stream.StreamChannel;
import com.xunmiw.article.stream.StreamService;
import com.xunmiw.pojo.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@EnableBinding(StreamChannel.class)
public class StreamServiceImpl implements StreamService {

    @Autowired
    private StreamChannel streamChannel;

    @Override
    public void publish() {
        AppUser appUser = new AppUser();
        appUser.setId("101");
        appUser.setNickname("xunmiw");

        streamChannel.output().send(MessageBuilder.withPayload(appUser).build());
    }

    @Override
    public void eat(String dumpling) {
        streamChannel.output().send(MessageBuilder.withPayload(dumpling).build());
    }
}
