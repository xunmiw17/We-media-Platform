package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticleControllerApi;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.enums.ArticleCoverType;
import com.xunmiw.enums.ArticleReviewStatus;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.ArticleBO;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
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

    @Override
    public GraceJSONResult queryMyList(String userId, String keyword, Integer status,
                                       Date startDate, Date endDate, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        // 查询我的列表，调用service
        PagedGridResult result = articleService.queryMyArticles(userId, keyword, status, startDate, endDate, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult queryAllList(Integer status, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = articleService.queryAllList(status, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult doReview(String articleId, Integer passOrNot) {
        if (passOrNot == YesOrNo.YES.type) {
            // 审核成功
            articleService.updateArticleStatus(articleId, ArticleReviewStatus.SUCCESS.type);
        } else if (passOrNot == YesOrNo.NO.type) {
            // 审核失败
            articleService.updateArticleStatus(articleId, ArticleReviewStatus.FAILED.type);
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        articleService.deleteArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.updateArticleStatus(articleId, ArticleReviewStatus.WITHDRAW.type);
        return GraceJSONResult.ok();
    }
}
