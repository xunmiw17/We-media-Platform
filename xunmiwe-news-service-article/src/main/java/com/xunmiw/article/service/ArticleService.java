package com.xunmiw.article.service;

import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.ArticleBO;

public interface ArticleService {

    /**
     * 发布文章
     * @param articleBO
     */
    void createArticle(ArticleBO articleBO);
}
