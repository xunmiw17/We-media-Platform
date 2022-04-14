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
}
