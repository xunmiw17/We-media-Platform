package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.ArticlePortalControllerApi;
import com.xunmiw.api.controller.user.UserControllerApi;
import com.xunmiw.article.service.ArticlePortalService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Article;
import com.xunmiw.pojo.eo.ArticleEO;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.pojo.vo.IndexArticleVO;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.GetQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private UserControllerApi userControllerApi;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public GraceJSONResult queryUserPortalArticlesES(String keyword, Integer category, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;
        // ES的页码从0开始计算，所以page需要 -1
        page--;

        Pageable pageable = PageRequest.of(page, pageSize);
        SearchQuery query = null;
        AggregatedPage<ArticleEO> pagedList = null;
        if (StringUtils.isBlank(keyword) && category == null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(pageable)
                    .withSort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC))
                    .build();
            pagedList = elasticsearchTemplate.queryForPage(query, ArticleEO.class);
        } else if (StringUtils.isBlank(keyword) && category != null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("categoryId", category))
                    .withPageable(pageable)
                    .build();
            pagedList = elasticsearchTemplate.queryForPage(query, ArticleEO.class);
        } else if (StringUtils.isNotBlank(keyword) && category == null) {
            String titleField = "title";
            String preTag = "<font color='red'>";
            String postTag = "</font>";
            // Highlight the title field; if using Elasticsearch 7x instead of current 6.8.6, the code would be much simpler.
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery(titleField, keyword))
                    .withHighlightFields(new HighlightBuilder.Field(titleField)
                            .preTags(preTag)
                            .postTags(postTag))
                    .withPageable(pageable)
                    .build();
            pagedList = elasticsearchTemplate.queryForPage(query, ArticleEO.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    SearchHits hits = searchResponse.getHits();
                    List<ArticleEO> highlightArticles = new ArrayList<>();
                    for (SearchHit hit : hits) {
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        String title = highlightField.getFragments()[0].toString();

                        // 获得其他字段信息数据，并且重新封装
                        String articleId = (String) hit.getSourceAsMap().get("id");
                        Integer categoryId = (Integer) hit.getSourceAsMap().get("categoryId");
                        Integer articleType = (Integer) hit.getSourceAsMap().get("articleType");
                        String articleCover = (String) hit.getSourceAsMap().get("articleCover");
                        String publishUserId = (String) hit.getSourceAsMap().get("publishUserId");
                        Long dateLong = (Long) hit.getSourceAsMap().get("publishTime");
                        Date publishTime = new Date(dateLong);

                        ArticleEO articleEO = new ArticleEO();
                        articleEO.setId(articleId);
                        articleEO.setCategoryId(categoryId);
                        articleEO.setTitle(title);
                        articleEO.setArticleType(articleType);
                        articleEO.setArticleCover(articleCover);
                        articleEO.setPublishUserId(publishUserId);
                        articleEO.setPublishTime(publishTime);

                        highlightArticles.add(articleEO);
                    }
                    return new AggregatedPageImpl<>((List<T>) highlightArticles, pageable, searchResponse.getHits().totalHits);
                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
        } else {
            String titleField = "title";
            String preTag = "<font color='red'>";
            String postTag = "</font>";
            // Highlight the title field; if using Elasticsearch 7x instead of current 6.8.6, the code would be much simpler.
            query = new NativeSearchQueryBuilder()
                    // .withQuery(QueryBuilders.matchQuery(titleField, keyword))
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.matchQuery(titleField, keyword))
                            .must(QueryBuilders.termQuery("categoryId", category)))
                    .withHighlightFields(new HighlightBuilder.Field(titleField)
                            .preTags(preTag)
                            .postTags(postTag))
                    .withPageable(pageable)
                    .build();
            pagedList = elasticsearchTemplate.queryForPage(query, ArticleEO.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    SearchHits hits = searchResponse.getHits();
                    List<ArticleEO> highlightArticles = new ArrayList<>();
                    for (SearchHit hit : hits) {
                        HighlightField highlightField = hit.getHighlightFields().get("title");
                        String title = highlightField.getFragments()[0].toString();

                        // 获得其他字段信息数据，并且重新封装
                        String articleId = (String) hit.getSourceAsMap().get("id");
                        Integer categoryId = (Integer) hit.getSourceAsMap().get("categoryId");
                        Integer articleType = (Integer) hit.getSourceAsMap().get("articleType");
                        String articleCover = (String) hit.getSourceAsMap().get("articleCover");
                        String publishUserId = (String) hit.getSourceAsMap().get("publishUserId");
                        Long dateLong = (Long) hit.getSourceAsMap().get("publishTime");
                        Date publishTime = new Date(dateLong);

                        ArticleEO articleEO = new ArticleEO();
                        articleEO.setId(articleId);
                        articleEO.setCategoryId(categoryId);
                        articleEO.setTitle(title);
                        articleEO.setArticleType(articleType);
                        articleEO.setArticleCover(articleCover);
                        articleEO.setPublishUserId(publishUserId);
                        articleEO.setPublishTime(publishTime);

                        highlightArticles.add(articleEO);
                    }
                    return new AggregatedPageImpl<>((List<T>) highlightArticles, pageable, searchResponse.getHits().totalHits);
                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
        }

        // 重组文章列表，以约定方式 (PagedGridResult of list of articles) 返回给前端
        List<ArticleEO> articleEOS = pagedList.getContent();
        List<Article> articles = new ArrayList<>();
        for (ArticleEO articleEO : articleEOS) {
            Article article = new Article();
            BeanUtils.copyProperties(articleEO, article);
            articles.add(article);
        }

        PagedGridResult result = new PagedGridResult();
        result.setRows(articles);
        result.setTotal(pagedList.getTotalPages());
        result.setRecords(pagedList.getTotalElements());
        result.setPage(page + 1);
        result = incorporateUserInfoInArticles(result);

        return GraceJSONResult.ok(result);
    }

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
        // List<Article> articles = articlePortalService.queryHotArticleList();

        // 从Redis中获取分值（阅读量）前五的文章
        Set<String> articleIds = redisOperator.zrevrange(REDIS_HOT_ARTICLE, 0, 4);
        // Elasticsearch根据ids查询热门文章
        List<Article> articles = new ArrayList<>();
        for (String articleId : articleIds) {
            GetQuery query = new GetQuery();
            query.setId(articleId);
            ArticleEO articleEO = elasticsearchTemplate.queryForObject(query, ArticleEO.class);
            Article article = new Article();
            BeanUtils.copyProperties(articleEO, article);
            articles.add(article);
        }
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
        if (!publisherList.isEmpty()) {
            articleDetailVO.setPublishUserName(publisherList.get(0).getNickname());
        }

        String readCountStr = redisOperator.get(REDIS_ARTICLE_READ_COUNT + ":" + articleId);
        if (StringUtils.isNotBlank(readCountStr))
            articleDetailVO.setReadCounts(Integer.valueOf(readCountStr));

        return GraceJSONResult.ok(articleDetailVO);
    }

    @Override
    public Integer readCounts(String articleId) {
        String readCountStr = redisOperator.get(REDIS_ARTICLE_READ_COUNT + ":" + articleId);
        return Integer.valueOf(readCountStr);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {

        String userIP = IPUtil.getRequestIp(request);
        // 将userIP存入Redis，防止用户刷阅读量
        redisOperator.setnx(REDIS_ARTICLE_ALREADY_READ + ":" + articleId + ":" + userIP, userIP);
        redisOperator.increment(REDIS_ARTICLE_READ_COUNT + ":" + articleId, 1);

        // 将阅读量作为文章score存入Zset，用于首页热闻展示阅读量排名
        redisOperator.zincrby(REDIS_HOT_ARTICLE, articleId, 1);
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

    /**
     * 发起远程调用，获得指定用户(们)的基本信息
     * @param userIds
     * @return
     */
    public List<AppUserVO> getUserList(Set<String> userIds) {

        // 硬编码服务地址 (最原始的方法)
        //  String getUserInfoUrl = "http://user.imoocnews.com:8003/user/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);

        // 通过Eureka服务发现DiscoveryClient动态获取服务地址 (比直接硬编码更好)
        // String serviceId = "SERVICE-USER";
        // List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        // ServiceInstance userServiceInstance = instances.get(0);
        // String getUserInfoUrl = "http://" + userServiceInstance.getHost() + ":" + userServiceInstance.getPort() + "/user"
        //        + "/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);

        // 直接通过serviceId得到服务URL地址 (相比DiscoveryClient更简洁)
        // String getUserInfoUrl = "http://" + serviceId + "/user/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);

        // 通过Feign实现远程调用 (相比使用serviceId硬编码更简洁)
        GraceJSONResult responseBody = userControllerApi.queryUserByIds(JsonUtils.objectToJson(userIds));

        // RestTemplate实现远程调用，可以 1) 直接硬编码 2) 通过DiscoveryClient获取服务host与port 3) 直接通过服务id硬编码
        // ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(getUserInfoUrl, GraceJSONResult.class);
        // GraceJSONResult responseBody = responseEntity.getBody();

        List<AppUserVO> users = null;
        // 注意这里使用equals方法进行两个Integer的比较，如果使用==，即使Integer的值相同，但Integer的地址不同，比较结果仍然为false
        if (responseBody.getStatus().equals(ResponseStatusEnum.SUCCESS.status())) {
            String usersJson = JsonUtils.objectToJson(responseBody.getData());
            users = JsonUtils.jsonToList(usersJson, AppUserVO.class);
        } else {
            users = new ArrayList<>();
        }
        return users;
    }
}
