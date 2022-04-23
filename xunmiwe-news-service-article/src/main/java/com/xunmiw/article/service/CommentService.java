package com.xunmiw.article.service;

import com.xunmiw.utils.PagedGridResult;

public interface CommentService {

    /**
     * 用户发表评论
     * @param articleId
     * @param fatherId
     * @param content
     * @param userId
     * @param nickname
     */
    void createComment(String articleId, String fatherId, String content, String userId, String nickname, String commentUserFace);

    /**
     * 分页查询文章所有评论
     * @param articleId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult listComments(String articleId, Integer page, Integer pageSize);

    /**
     * 分页查询用户历史评论
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    PagedGridResult mng(String writerId, Integer page, Integer pageSize);

    /**
     * 删除某条评论
     * @param commentId
     */
    void delete(String commentId, String writerId);
}
