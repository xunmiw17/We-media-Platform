package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticlePortalControllerApi;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.pojo.vo.IndexArticleVO;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.PagedGridResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public GraceJSONResult queryUserPortalArticles(String keyword, Integer category, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = articlePortalService.queryUserPortalArticles(keyword, category, page, pageSize);

        List<Article> rows = (List<Article>) result.getRows();
        // 1. 构建发布者id列表，用于查询每个文章所对应的user信息（包括nickname，头像）页面显示
        Set<String> ids = new HashSet<>();
        for (Article article : rows) {
            ids.add(article.getPublishUserId());
        }

        // 2. 发起远程调用，请求user服务获得对应user（发布者）列表
        String getUserInfoUrl = "http://user.imoocnews.com:8003/user/queryUserByIds?userIds=" + JsonUtils.objectToJson(ids);
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(getUserInfoUrl, GraceJSONResult.class);
        GraceJSONResult responseBody = responseEntity.getBody();

        // 注意这里使用equals方法进行两个Integer的比较，如果使用==，即使Integer的值相同，但Integer的地址不同，比较结果仍然为false
        if (!responseBody.getStatus().equals(ResponseStatusEnum.SUCCESS.status())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        }
        String publishersJson = JsonUtils.objectToJson(responseBody.getData());
        List<AppUserVO> publishers = JsonUtils.jsonToList(publishersJson, AppUserVO.class);

        // 3. 拼接两个List，重组文章列表
        List<IndexArticleVO> indexArticleVOs = new ArrayList<>();
        for (Article article : rows) {
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            BeanUtils.copyProperties(article, indexArticleVO);
            String userId = article.getPublishUserId();
            AppUserVO publisherVO = null;
            for (AppUserVO appUserVO : publishers) {
                if (appUserVO.getId().equals(userId)) {
                    publisherVO = appUserVO;
                    break;
                }
            }
            indexArticleVO.setPublisherVO(publisherVO);
            indexArticleVOs.add(indexArticleVO);
        }

        result.setRows(indexArticleVOs);

        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult queryHotArticleList() {
        List<Article> articles = articlePortalService.queryHotArticleList();
        return GraceJSONResult.ok(articles);
    }
}
