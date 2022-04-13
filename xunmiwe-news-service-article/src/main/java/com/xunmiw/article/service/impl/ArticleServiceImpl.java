package com.xunmiw.article.service.impl;

import com.xunmiw.article.mapper.ArticleMapper;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.enums.ArticleAppointType;
import com.xunmiw.enums.ArticleReviewStatus;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.ArticleBO;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

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
}
