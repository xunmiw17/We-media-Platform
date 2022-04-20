package com.xunmiw.article.service;

import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.utils.PagedGridResult;

import java.util.List;

public interface ArticlePortalService {

    /**
     * 用户portal根据文章类别分页查询文章列表
     * @param page
     * @param pageSize
     * @param keyword
     * @param category
     * @return
     */
    PagedGridResult queryUserPortalArticles(String keyword, Integer category, Integer page, Integer pageSize);

    /**
     * 查询热闻
     * @return
     */
    List<Article> queryHotArticleList();

    /**
     * 用户portal个人主页查询用户文章列表
     * @return
     */
    PagedGridResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize);

    /**
     * 用户portal个人主页查询用户近期佳文
     * @param writerId
     * @return
     */
    PagedGridResult queryGoodArticleListOfWriter(String writerId);

    /**
     * 查询文章详情
     * @param articleId
     * @return
     */
    ArticleDetailVO detail(String articleId);
}
