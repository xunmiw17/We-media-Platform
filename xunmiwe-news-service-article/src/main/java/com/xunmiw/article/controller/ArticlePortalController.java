package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticlePortalControllerApi;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.pojo.vo.IndexArticleVO;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        // 将结果的Article List转换为同时带有Article和Publisher信息的VO
        result = incorporateUserInfoInArticles(result);

        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult queryHotArticleList() {
        List<Article> articles = articlePortalService.queryHotArticleList();
        return GraceJSONResult.ok(articles);
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = articlePortalService.queryArticleListOfWriter(writerId, page, pageSize);
        result = incorporateUserInfoInArticles(result);

        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {
        PagedGridResult result = articlePortalService.queryGoodArticleListOfWriter(writerId);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult detail(String articleId) {
        ArticleDetailVO articleDetailVO = articlePortalService.detail(articleId);

        Set<String> set = new HashSet<>();
        set.add(articleDetailVO.getPublishUserId());
        List<AppUserVO> publisherList = getUserList(set);
        articleDetailVO.setPublishUserName(publisherList.get(0).getNickname());

        String readCountStr = redisOperator.get(REDIS_ARTICLE_READ_COUNT + ":" + articleId);
        if (StringUtils.isNotBlank(readCountStr))
            articleDetailVO.setReadCounts(Integer.valueOf(readCountStr));

        return GraceJSONResult.ok(articleDetailVO);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {

        String userIP = IPUtil.getRequestIp(request);
        // 将userIP存入Redis，防止用户刷阅读量
        redisOperator.setnx(REDIS_ARTICLE_ALREADY_READ + ":" + articleId + ":" + userIP, userIP);

        redisOperator.increment(REDIS_ARTICLE_READ_COUNT + ":" + articleId, 1);
        return GraceJSONResult.ok();
    }

    private PagedGridResult incorporateUserInfoInArticles(PagedGridResult result) {
        List<Article> rows = (List<Article>) result.getRows();
        // 1. 构建发布者id列表，用于查询每个文章所对应的user信息（包括nickname，头像）页面显示
        Set<String> ids = new HashSet<>();
        List<String> articleIds = new ArrayList<>();
        for (Article article : rows) {
            // 1.1 构建发布者的set
            ids.add(article.getPublishUserId());
            // 1.2 构建文章id的list
            articleIds.add(REDIS_ARTICLE_READ_COUNT + ":" + article.getId());
        }

        // 发起Redis批量查询mget，获取文章阅读量的List
        List<String> readCounts = redisOperator.mget(articleIds);

        // 2. 发起远程调用，请求user服务获得对应user（发布者）列表
        List<AppUserVO> publishers = getUserList(ids);

        // 3. 拼接两个List，重组文章列表
        List<IndexArticleVO> indexArticleVOs = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            Article article = rows.get(i);
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            BeanUtils.copyProperties(article, indexArticleVO);
            String userId = article.getPublishUserId();

            // 3.1 获得文章发布者信息
            AppUserVO publisherVO = null;
            for (AppUserVO appUserVO : publishers) {
                if (appUserVO.getId().equals(userId)) {
                    publisherVO = appUserVO;
                    break;
                }
            }
            indexArticleVO.setPublisherVO(publisherVO);

            // 3.2 设置文章阅读量
            String readCountStr = readCounts.get(i);
            if (StringUtils.isNotBlank(readCountStr))
                indexArticleVO.setReadCounts(Integer.valueOf(readCountStr));

            indexArticleVOs.add(indexArticleVO);
        }

        result.setRows(indexArticleVOs);
        return result;
    }
}
