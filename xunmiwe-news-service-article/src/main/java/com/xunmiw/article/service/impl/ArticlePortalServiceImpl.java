package com.xunmiw.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.article.mapper.ArticleMapper;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.enums.ArticleReviewStatus;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ArticlePortalServiceImpl extends BaseService implements ArticlePortalService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PagedGridResult queryUserPortalArticles(String keyword, Integer category, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("publishTime").desc();
        Example.Criteria criteria = example.createCriteria();
        if (category != null) {
            criteria.andEqualTo("categoryId", category);
        }
        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);
        return setPagedGrid(articles, page);
    }

    @Override
    public List<Article> queryHotArticleList() {
        Example example = new Example(Article.class);
        example.orderBy("publishTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        PageHelper.startPage(1, 5);
        List<Article> articles = articleMapper.selectByExample(example);

        return articles;
    }

    @Override
    public PagedGridResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("publishTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("publishUserId", writerId);
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);

        PagedGridResult result = setPagedGrid(articles, page);
        return result;
    }

    @Override
    public PagedGridResult queryGoodArticleListOfWriter(String writerId) {
        Example example = new Example(Article.class);
        example.orderBy("publishTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("publishUserId", writerId);
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        PageHelper.startPage(1, 5);
        List<Article> articles = articleMapper.selectByExample(example);
        PagedGridResult result = setPagedGrid(articles, 1);

        return result;
    }

    @Override
    public ArticleDetailVO detail(String articleId) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", articleId);
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        Article article = articleMapper.selectOneByExample(example);

        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        BeanUtils.copyProperties(article, articleDetailVO);

        return articleDetailVO;
    }
}
