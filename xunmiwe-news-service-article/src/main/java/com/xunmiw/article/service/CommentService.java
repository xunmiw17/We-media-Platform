package com.xunmiw.article.service;

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
}
