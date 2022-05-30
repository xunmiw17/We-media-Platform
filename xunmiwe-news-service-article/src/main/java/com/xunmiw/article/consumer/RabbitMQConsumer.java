package com.xunmiw.article.consumer;

import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.article.mapper.ArticleMapper;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.eo.ArticleEO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @RabbitListener(queues = {RabbitMQConfig.ARTICLE_DELAYED_PUBLISH_QUEUE})
    public void listen(String payload, Message message) {
        String articleId = payload;

        // 更新数据库中appoint字段为立即发布
        articleService.updateArticleAppointStatus(articleId);

        // 将article存入Elasticsearch
        Article article = articleMapper.selectByPrimaryKey(articleId);
        ArticleEO articleEO = new ArticleEO();
        BeanUtils.copyProperties(article, articleEO);
        IndexQuery query = new IndexQueryBuilder()
                .withObject(articleEO)
                .build();
        elasticsearchTemplate.index(query);
    }
}
