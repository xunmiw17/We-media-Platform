package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticlePortalControllerApi;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {

    @Autowired
    private ArticlePortalService articlePortalService;

    @Override
    public GraceJSONResult queryUserPortalArticles(String keyword, Integer category, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = articlePortalService.queryUserPortalArticles(keyword, category, page, pageSize);
        return GraceJSONResult.ok(result);
    }
}
