package com.xunmiw.article.service;

import com.xunmiw.pojo.bo.ArticleBO;
import com.xunmiw.utils.PagedGridResult;

import java.util.Date;

public interface ArticleService {

    /**
     * 发布文章
     * @param articleBO
     */
    void createArticle(ArticleBO articleBO);

    /**
     * 发布定时发布的文章
     */
    void publishAppointedArticles();

    /**
     * 查询我的文章列表
     * @param userId
     * @param keyword
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryMyArticles(String userId, String keyword, Integer status,
                                    Date startDate, Date endDate, Integer page, Integer pageSize);
}
