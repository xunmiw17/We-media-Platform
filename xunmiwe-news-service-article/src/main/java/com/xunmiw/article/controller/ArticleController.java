package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticleControllerApi;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.enums.ArticleCoverType;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.ArticleBO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ArticleController extends BaseController implements ArticleControllerApi {

    @Autowired
    private ArticleService articleService;

    @Override
    public GraceJSONResult createArticle(ArticleBO articleBO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = getErrors(bindingResult);
            return GraceJSONResult.errorMap(errors);
        }

        // 判断文章封面类型，单图必填，纯文字则设置为空
        if (articleBO.getArticleType() == ArticleCoverType.ONE_IMAGE.type) {
            if (StringUtils.isBlank(articleBO.getArticleCover()))
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
        } else if (articleBO.getArticleType() == ArticleCoverType.WORDS.type) {
            articleBO.setArticleCover("");
        }

        // 判断文章分类是否存在
        String jsonCategories = redisOperator.get(REDIS_CATEGORY_LIST);
        if (StringUtils.isBlank(jsonCategories)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        Category category = checkCategory(jsonCategories, articleBO.getCategoryId());
        if (category == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_CATEGORY_NOT_EXIST_ERROR);
        }
        articleService.createArticle(articleBO);
        return GraceJSONResult.ok();
    }
}
