package com.xunmiw.api.controller.article;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.CommentReplyBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "文章评论业务的Controller", tags = {"文章评论业务的Controller"})
@RequestMapping("comment")
public interface CommentControllerApi {

    @ApiOperation(value = "评论文章", notes = "评论文章", httpMethod = "POST")
    @PostMapping("createComment")
    public GraceJSONResult createComment(@RequestBody @Valid CommentReplyBO commentReplyBO,
                                         BindingResult bindingResult);

    @ApiOperation(value = "查询评论数", notes = "查询评论数", httpMethod = "GET")
    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String articleId);

    @ApiOperation(value = "分页查询文章所有评论", notes = "分页查询文章所有评论", httpMethod = "GET")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String articleId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize);
}
