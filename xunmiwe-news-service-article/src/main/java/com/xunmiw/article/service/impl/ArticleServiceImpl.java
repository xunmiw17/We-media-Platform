package com.xunmiw.article.service.impl;

import com.github.pagehelper.PageHelper;
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
import com.xunmiw.utils.PagedGridResult;
import com.xunmiw.utils.extend.AliTextReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

        // // 通过阿里智能AI实现对文章文本的自动检测
        // String reviewTextResult = aliTextReviewUtils.reviewTextContent(articleBO.getContent());
        // if (reviewTextResult.equals(ArticleReviewLevel.PASS.type)) {
        //     // 修改文章状态为审核通过
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.SUCCESS.type);
        // } else if (reviewTextResult.equals(ArticleReviewLevel.REVIEW.type)) {
        //     // 修改文章状态为需要人工审核
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.WAITING_MANUAL.type);
        // } else if (reviewTextResult.equals(ArticleReviewLevel.BLOCK.type)) {
        //     // 修改文章状态为审核未通过
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.FAILED.type);
        // }

        // 模拟阿里智能文本检测，随机生成文章状态，用于测试
        // Random random = new Random();
        // double rand = random.nextDouble();
        // if (rand < 0.15) {
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.FAILED.type);
        // } else if (rand < 0.35) {
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.WAITING_MANUAL.type);
        // } else {
        //     updateArticleStatus(article.getId(), ArticleReviewStatus.SUCCESS.type);
        // }

        // 不进行AI文本检测，直接进入人工审核
        updateArticleStatus(article.getId(), ArticleReviewStatus.WAITING_MANUAL.type);
    }

    @Override
    @Transactional
    public void publishAppointedArticles() {
        articleMapperCustom.publishAppointedArticles();
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
        article.setIsDelete(YesOrNo.YES.type);

        int result = articleMapper.updateByExampleSelective(article, example);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }
    }

}
