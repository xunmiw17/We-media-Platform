package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.CommentControllerApi;
import com.xunmiw.article.service.CommentService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.CommentReplyBO;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class CommentController extends BaseController implements CommentControllerApi {

    @Autowired
    private CommentService commentService;

    @Override
    public GraceJSONResult createComment(CommentReplyBO commentReplyBO, BindingResult bindingResult) {

        // 0. 判断验证信息
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = getErrors(bindingResult);
            return GraceJSONResult.errorMap(errors);
        }

        // 1. 根据留言用户Id查询用户信息，用于放入Comment表中做冗余处理，避免查询Comment时进行多表关联
        String userId = commentReplyBO.getCommentUserId();

        // 2. 发起远程调用，获得用户nickname
        Set<String> ids = new HashSet<>();
        ids.add(userId);
        List<AppUserVO> users = getUserList(ids);
        AppUserVO appUserVO = users.get(0);
        String nickname = appUserVO.getNickname();
        String commentUserFace = appUserVO.getFace();

        // 3. 保存用户评论信息到数据库
        commentService.createComment(commentReplyBO.getArticleId(), commentReplyBO.getFatherId(),
                                            commentReplyBO.getContent(), userId, nickname, commentUserFace);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult counts(String articleId) {
        String commentCountStr = redisOperator.get(REDIS_ARTICLE_COMMENT_COUNT + ":" + articleId);
        Integer count = 0;
        if (StringUtils.isNotBlank(commentCountStr))
            count = Integer.valueOf(commentCountStr);
        return GraceJSONResult.ok(count);
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = commentService.listComments(articleId, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = commentService.mng(writerId, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        commentService.delete(commentId, writerId);
        return GraceJSONResult.ok();
    }
}
