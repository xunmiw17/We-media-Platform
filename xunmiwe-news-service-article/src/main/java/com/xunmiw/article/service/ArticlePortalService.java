package com.xunmiw.article.service;

import com.xunmiw.utils.PagedGridResult;

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
}
