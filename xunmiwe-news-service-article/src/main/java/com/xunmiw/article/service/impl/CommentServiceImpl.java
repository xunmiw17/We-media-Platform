package com.xunmiw.article.service.impl;

import com.xunmiw.api.service.BaseService;
import com.xunmiw.article.mapper.CommentsMapper;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.article.service.CommentService;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Comments;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CommentServiceImpl extends BaseService implements CommentService {

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private Sid sid;

    @Override
    @Transactional
    public void createComment(String articleId, String fatherId, String content, String userId, String nickname, String commentUserFace) {

        ArticleDetailVO articleDetailVO = articlePortalService.detail(articleId);

        Comments comments = new Comments();

        comments.setId(sid.nextShort());
        comments.setFatherId(fatherId);

        comments.setCommentUserId(userId);
        comments.setCommentUserNickname(nickname);
        comments.setCommentUserFace(commentUserFace);
        comments.setContent(content);

        comments.setArticleId(articleId);
        comments.setArticleCover(articleDetailVO.getCover());
        comments.setArticleTitle(articleDetailVO.getTitle());
        comments.setWriterId(articleDetailVO.getPublishUserId());

        comments.setCreateTime(new Date());

        int result = commentsMapper.insert(comments);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }
    }
}
