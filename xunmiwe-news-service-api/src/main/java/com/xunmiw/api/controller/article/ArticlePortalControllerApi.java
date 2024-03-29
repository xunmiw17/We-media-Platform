package com.xunmiw.api.controller.article;

import com.xunmiw.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Api(value = "用户portal查询文章的Controller", tags = {"用户portal查询文章的Controller"})
@RequestMapping("portal/article")
public interface ArticlePortalControllerApi {

    @ApiOperation(value = "用户portal通过ES根据类别分页查询文章列表", notes = "用户portal通过ES根据类别分页查询文章列表", httpMethod = "GET")
    @GetMapping("es/list")
    public GraceJSONResult queryUserPortalArticlesES(@RequestParam String keyword,
                                                   @RequestParam Integer category,
                                                   @RequestParam Integer page,
                                                   @RequestParam Integer pageSize);

    @ApiOperation(value = "用户portal根据类别分页查询文章列表", notes = "用户portal根据类别分页查询文章列表", httpMethod = "GET")
    @GetMapping("list")
    public GraceJSONResult queryUserPortalArticles(@RequestParam String keyword,
                                                   @RequestParam Integer category,
                                                   @RequestParam Integer page,
                                                   @RequestParam Integer pageSize);

    @ApiOperation(value = "用户portal查询热闻", notes = "用户portal查询热闻", httpMethod = "GET")
    @GetMapping("hotList")
    public GraceJSONResult queryHotArticleList();

    @ApiOperation(value = "用户portal个人主页查询用户文章列表", notes = "用户portal个人主页查询用户文章列表", httpMethod = "GET")
    @GetMapping("queryArticleListOfWriter")
    public GraceJSONResult queryArticleListOfWriter(@RequestParam String writerId,
                                                    @RequestParam Integer page,
                                                    @RequestParam Integer pageSize);

    @ApiOperation(value = "用户portal个人主页查询用户近期佳文", notes = "用户portal个人主页查询用户近期佳文", httpMethod = "GET")
    @GetMapping("queryGoodArticleListOfWriter")
    public GraceJSONResult queryGoodArticleListOfWriter(@RequestParam String writerId);

    @ApiOperation(value = "查询文章详情", notes = "查询文章详情", httpMethod = "GET")
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam String articleId);

    @ApiOperation(value = "获得文章阅读数", notes = "获得文章阅读数", httpMethod = "GET")
    @GetMapping("readCounts")
    public Integer readCounts(@RequestParam String articleId);

    @ApiOperation(value = "用户阅读文章", notes = "用户阅读文章", httpMethod = "POST")
    @PostMapping("readArticle")
    public GraceJSONResult readArticle(@RequestParam String articleId, HttpServletRequest request);
}
