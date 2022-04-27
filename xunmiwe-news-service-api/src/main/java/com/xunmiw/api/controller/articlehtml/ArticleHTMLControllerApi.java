package com.xunmiw.api.controller.articlehtml;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "文章静态化业务的Controller", tags = {"文章静态化业务的Controller"})
@RequestMapping("article/html")
public interface ArticleHTMLControllerApi {

    @ApiOperation(value = "从GridFS中下载已上传的文章HTML", notes = "从GridFS中下载已上传的文章HTML", httpMethod = "GET")
    @GetMapping("download")
    public Integer download(@RequestParam String articleId, @RequestParam String fileId) throws Exception;
}
