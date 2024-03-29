package com.xunmiw.api.controller.article;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.ArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Date;

@Api(value = "文章业务的Controller", tags = {"文章业务的Controller"})
@RequestMapping("article")
public interface ArticleControllerApi {

    @ApiOperation(value = "用户发布文章", notes = "用户发布文章", httpMethod = "POST")
    @PostMapping("createArticle")
    public GraceJSONResult createArticle(@RequestBody @Valid ArticleBO articleBO);

    @ApiOperation(value = "查询用户发表的文章列表", notes = "查询用户发表的文章列表", httpMethod = "POST")
    @PostMapping("queryMyList")
    public GraceJSONResult queryMyList(@RequestParam String userId,
                                       @RequestParam String keyword,
                                       @RequestParam Integer status,
                                       @RequestParam Date startDate,
                                       @RequestParam Date endDate,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize);

    @ApiOperation(value = "用于admin显示文章列表", notes = "用于admin显示文章列表", httpMethod = "POST")
    @PostMapping("queryAllList")
    public GraceJSONResult queryAllList(@RequestParam Integer status,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize);

    @ApiOperation(value = "admin文章审核", notes = "admin文章审核", httpMethod = "POST")
    @PostMapping("doReview")
    public GraceJSONResult doReview(@RequestParam String articleId, @RequestParam Integer passOrNot);

    @ApiOperation(value = "删除文章", notes = "删除文章", httpMethod = "POST")
    @PostMapping("delete")
    public GraceJSONResult delete(@RequestParam String userId, @RequestParam String articleId);

    @ApiOperation(value = "撤回文章", notes = "撤回文章", httpMethod = "POST")
    @PostMapping("withdraw")
    public GraceJSONResult withdraw(@RequestParam String userId, @RequestParam String articleId);


}
