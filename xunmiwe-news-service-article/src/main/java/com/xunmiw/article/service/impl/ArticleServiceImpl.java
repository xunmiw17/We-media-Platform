package com.xunmiw.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.mongodb.client.gridfs.GridFSBucket;
import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.article.mapper.ArticleMapper;
import com.xunmiw.article.mapper.ArticleMapperCustom;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.enums.ArticleAppointType;
import com.xunmiw.enums.ArticleReviewStatus;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.bo.ArticleBO;
import com.xunmiw.pojo.eo.ArticleEO;
import com.xunmiw.utils.PagedGridResult;
import com.xunmiw.utils.extend.AliTextReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.n3r.idworker.Sid;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleMapperCustom articleMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private AliTextReviewUtils aliTextReviewUtils;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    @Transactional
    public void createArticle(ArticleBO articleBO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleBO, article);
        article.setId(sid.nextShort());
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setReadCounts(0);
        article.setCommentCounts(0);
        article.setIsDelete(YesOrNo.NO.type);
        if (articleBO.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
            article.setPublishTime(new Date());
        }
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());
        int result = articleMapper.insert(article);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }

        // 计算定时发布时间，发送延迟消息
        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {

            // 计算延迟发布时间
            Date end = article.getPublishTime();
            Date start = new Date();
            int delay = (int) (end.getTime() - start.getTime());

            // 设置messagePostProcessor，指定延迟时间
            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    message.getMessageProperties().setDelay(delay);
                    return message;
                }
            };

            rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_DELAYED_PUBLISH_EXCHANGE, "delay.publish.article", article.getId(), messagePostProcessor);
        }

        // 不进行AI文本检测，直接进入人工审核
        updateArticleStatus(article.getId(), ArticleReviewStatus.WAITING_MANUAL.type);
    }

    @Override
    @Transactional
    public void publishAppointedArticles() {
        articleMapperCustom.publishAppointedArticles();
    }

    @Override
    @Transactional
    public void updateArticleAppointStatus(String articleId) {
        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(ArticleAppointType.IMMEDIATELY.type);

        articleMapper.updateByPrimaryKeySelective(article);
    }

    @Override
    public PagedGridResult queryMyArticles(String userId, String keyword, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }
        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("publishTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("publishTime", endDate);
        }
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);
        return setPagedGrid(articles, page);
    }

    @Override
    @Transactional
    public void updateArticleStatus(String articleId, Integer pendingStatus) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", articleId);

        Article article = new Article();
        article.setArticleStatus(pendingStatus);

        int result = articleMapper.updateByExampleSelective(article, example);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
    }

    @Override
    public PagedGridResult queryAllList(Integer status, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);

        return setPagedGrid(articles, page);
    }

    @Override
    public void deleteArticle(String userId, String articleId) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        criteria.andEqualTo("id", articleId);

        Article article = new Article();
        // 逻辑删除
        article.setIsDelete(YesOrNo.YES.type);

        int result = articleMapper.updateByExampleSelective(article, example);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }
    }

    @Override
    @Transactional
    public void createArticleFileId(String articleId, String fileId) {
        Article article = new Article();
        article.setId(articleId);
        article.setMongoFileId(fileId);

        int result = articleMapper.updateByPrimaryKeySelective(article);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public void deleteArticleFromGridFS(String articleId) {
        Article article = new Article();
        article.setId(articleId);

        Article target = articleMapper.selectOne(article);
        String fileId = target.getMongoFileId();
        ObjectId objectId = new ObjectId(fileId);
        gridFSBucket.delete(objectId);
    }

    @Override
    public void storeES(String articleId) {
        Article articleExp = new Article();
        articleExp.setId(articleId);

        Article article = articleMapper.selectOne(articleExp);
        if (article.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
            ArticleEO articleEO = new ArticleEO();
            BeanUtils.copyProperties(article, articleEO);

            IndexQuery query = new IndexQueryBuilder()
                    .withObject(articleEO)
                    .build();
            elasticsearchTemplate.index(query);
        } else {
            // TODO: Hand the task to MQ
        }
    }

    @Override
    public void deleteArticleFromES(String articleId) {
        elasticsearchTemplate.delete(ArticleEO.class, articleId);
    }
}
