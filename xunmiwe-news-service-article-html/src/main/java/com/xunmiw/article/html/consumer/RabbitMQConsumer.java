package com.xunmiw.article.html.consumer;

import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.article.html.component.ArticleDownloadAndDeleteComponent;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    @Autowired
    private ArticleDownloadAndDeleteComponent articleDownloadAndDeleteComponent;

    /**
     * 监听文章下载或删除事件，从GridFS中下载article服务生成的文章静态HTML页面或删除在前端代码中的对应文章HTML页面
     * @param payload
     * @param message
     */
    @RabbitListener(queues = {RabbitMQConfig.ARTICLE_DOWNLOAD_QUEUE})
    public void listen(String payload, Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        if (routingKey.equals("article.download")) {
            String[] ids = payload.split(",");
            String articleId = ids[0];
            String fileId = ids[1];

            try {
                int status = articleDownloadAndDeleteComponent.download(articleId, fileId);
                if (status != HttpStatus.OK.value()) {
                    GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (routingKey.equals("article.delete")) {
            String articleId = payload;

            Integer status = articleDownloadAndDeleteComponent.delete(articleId);
            if (status != HttpStatus.OK.value()) {
                GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
            }
        }
    }
}
