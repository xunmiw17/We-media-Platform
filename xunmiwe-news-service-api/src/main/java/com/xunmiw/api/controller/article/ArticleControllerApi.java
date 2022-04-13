package com.xunmiw.api.controller.article;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.ArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Api(value = "文章业务的Controller", tags = {"文章业务的Controller"})
@RequestMapping("article")
public interface ArticleControllerApi {

    @ApiOperation(value = "用户发布文章", notes = "用户发布文章", httpMethod = "POST")
    @PostMapping("createArticle")
    public GraceJSONResult createArticle(@RequestBody @Valid ArticleBO articleBO, BindingResult bindingResult);
}
