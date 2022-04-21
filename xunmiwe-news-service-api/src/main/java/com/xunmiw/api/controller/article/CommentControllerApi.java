package com.xunmiw.api.controller.article;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.CommentReplyBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Api(value = "文章评论业务的Controller", tags = {"文章评论业务的Controller"})
@RequestMapping("comment")
public interface CommentControllerApi {

    @ApiOperation(value = "评论文章", notes = "评论文章", httpMethod = "POST")
    @PostMapping("createComment")
    public GraceJSONResult createComment(@RequestBody @Valid CommentReplyBO commentReplyBO,
                                         BindingResult bindingResult);
}
