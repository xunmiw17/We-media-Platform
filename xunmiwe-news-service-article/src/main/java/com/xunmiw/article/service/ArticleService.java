package com.xunmiw.article.service;

import com.xunmiw.pojo.bo.ArticleBO;
import com.xunmiw.utils.PagedGridResult;

import java.util.Date;

public interface ArticleService {

    /**
     * 发布文章
     *
     * @param articleBO
     */
    void createArticle(ArticleBO articleBO);

    /**
     * 发布定时发布的文章
     */
    void publishAppointedArticles();

    /**
     * 查询我的文章列表
     *
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

    /**
     * 更改文章状态
     *
     * @param articleId
     * @param pendingStatus
     */
    public void updateArticleStatus(String articleId, Integer pendingStatus);

    /**
     * 用于admin查询所有文章列表
     *
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult queryAllList(Integer status, Integer page, Integer pageSize);

    /**
     * 删除文章
     *
     * @param userId
     * @param articleId
     */
    void deleteArticle(String userId, String articleId);

    /**
     * 将上传至GridFS的HTML文件Id关联到对应文章
     * @param articleId
     * @param fileId
     */
    void createArticleFileId(String articleId, String fileId);

    /**
     * 从GridFS中删除文章
     * @param articleId
     */
    void deleteArticleFromGridFS(String articleId);
}
