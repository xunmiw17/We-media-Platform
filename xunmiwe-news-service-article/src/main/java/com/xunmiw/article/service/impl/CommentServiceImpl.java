package com.xunmiw.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.article.mapper.CommentsMapper;
import com.xunmiw.article.mapper.CommentsMapperCustom;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.article.service.CommentService;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Comments;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.pojo.vo.CommentsVO;
import com.xunmiw.utils.PagedGridResult;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseService implements CommentService {

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentsMapperCustom commentsMapperCustom;

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

        // 评论数累加
        redisOperator.increment(REDIS_ARTICLE_COMMENT_COUNT + ":" + articleId, 1);
    }

    @Override
    public PagedGridResult listComments(String articleId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", articleId);

        PageHelper.startPage(page, pageSize);
        List<CommentsVO> commentsVOS = commentsMapperCustom.queryComments(map);
        PagedGridResult result = setPagedGrid(commentsVOS, page);
        return result;
    }

    @Override
    public PagedGridResult mng(String writerId, Integer page, Integer pageSize) {
        Example example = new Example(Comments.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("commentUserId", writerId);

        PageHelper.startPage(page, pageSize);
        List<Comments> comments = commentsMapper.selectByExample(example);
        return setPagedGrid(comments, page);
    }

    @Override
    public void delete(String commentId, String writerId) {
        Comments comments = new Comments();
        comments.setId(commentId);
        comments.setWriterId(writerId);

        int result = commentsMapper.delete(comments);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }
    }
}
