package com.xunmiw.article.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.xunmiw.api.BaseController;
import com.xunmiw.api.config.RabbitMQConfig;
import com.xunmiw.api.controller.article.ArticleControllerApi;
import com.xunmiw.article.service.ArticleService;
import com.xunmiw.enums.ArticleCoverType;
import com.xunmiw.enums.ArticleReviewStatus;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.ArticleBO;
import com.xunmiw.pojo.vo.ArticleDetailVO;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.PagedGridResult;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ArticleController extends BaseController implements ArticleControllerApi {

    @Autowired
    private ArticleService articleService;

    @Value("${freemarker.html.article}")
    private String articlePath;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public GraceJSONResult createArticle(ArticleBO articleBO) {
        // 判断文章封面类型，单图必填，纯文字则设置为空
        if (articleBO.getArticleType() == ArticleCoverType.ONE_IMAGE.type) {
            if (StringUtils.isBlank(articleBO.getArticleCover()))
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
        } else if (articleBO.getArticleType() == ArticleCoverType.WORDS.type)
            articleBO.setArticleCover("");

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

        if (passOrNot == YesOrNo.YES.type) {
            // 审核成功，生成文章详情页静态html
            try {
                // createArticleHTML(articleId);
                String fileId = createArticleHTMLToGridFS(articleId);
                // 将文件id存储到对应文章关联保存
                articleService.createArticleFileId(articleId, fileId);
                // 下载已上传到GridFS的文章静态HTML
                // downloadHTML(articleId, fileId);

                // 发布消息，传入articleId和fileId让article-html服务监听并下载HTML页面
                publishMessageDownloadingHTML(articleId, fileId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return GraceJSONResult.ok();
    }

    /**
     * 文章生成HTML
     * @param articleId
     * @throws Exception
     */
    private void createArticleHTML(String articleId) throws Exception {
        // 0. 配置freemarker基本环境
        Configuration config = new Configuration(Configuration.getVersion());
        // 声明freemarker模板所需要加载的目录的位置
        String classPath = this.getClass().getResource("/").getPath();
        config.setDirectoryForTemplateLoading(new File(classPath + "templates"));

        // 1. 获得现有的模板ftl文件
        Template template = config.getTemplate("detail.ftl", "utf-8");

        // 2. 获得文章详情数据
        ArticleDetailVO articleDetailVO = getArticleDetail(articleId);
        System.out.println("=======================" + articleDetailVO);
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", articleDetailVO);

        // 3. 融合动态数据和ftl，生成html
        File temp = new File(articlePath);
        if (!temp.exists()) {
            temp.mkdirs();
        }

        Writer out = new FileWriter(articlePath + File.separator + articleId + ".html");
        template.process(map, out);
        out.close();
    }

    /**
     * 文章生成HTML，上传至GridFS
     * @param articleId
     * @throws Exception
     */
    private String createArticleHTMLToGridFS(String articleId) throws Exception {
        Configuration config = new Configuration(Configuration.getVersion());
        String classPath = this.getClass().getResource("/").getPath();
        config.setDirectoryForTemplateLoading(new File(classPath + "templates"));

        Template template = config.getTemplate("detail.ftl", "utf-8");

        ArticleDetailVO articleDetailVO = getArticleDetail(articleId);
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", articleDetailVO);

        String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        InputStream is = IOUtils.toInputStream(htmlContent);

        ObjectId fileId = gridFSBucket.uploadFromStream(articleId + ".html", is);
        return fileId.toString();
    }

    /**
     * 原本用于article服务直接发起远程调用使article-html服务从GridFS中下载HTML页面，现被消息队列替换
     * （见publishMessageDownloadingHTML方法）
     * @param articleId
     * @param fileId
     */
    @Deprecated
    private void downloadHTML(String articleId, String fileId) {
        String downloadURL = "http://html.imoocnews.com:8002/article/html/download?articleId="
                + articleId + "&fileId=" + fileId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(downloadURL, Integer.class);
        int status = responseEntity.getBody();
        if (status != HttpStatus.OK.value()) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
    }

    private void publishMessageDownloadingHTML(String articleId, String fileId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, "article.download", articleId + "," + fileId);
    }

    /**
     * 根据文章id发起远程调用（本服务的另一个Controller），获得文章详情数据
     * @param articleId
     * @return
     */
    private ArticleDetailVO getArticleDetail(String articleId) {
        String articleDetailUrl = "http://www.imoocnews.com:8001/portal/article/detail?articleId=" + articleId;
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(articleDetailUrl, GraceJSONResult.class);
        GraceJSONResult result = responseEntity.getBody();
        ArticleDetailVO articleDetailVO = null;
        if (result.getStatus().equals(ResponseStatusEnum.SUCCESS.status())) {
            String jsonDetails = JsonUtils.objectToJson(result.getData());
            articleDetailVO = JsonUtils.jsonToPojo(jsonDetails, ArticleDetailVO.class);
        }
        return articleDetailVO;
    }

    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        // 从GridFS中删除文章
        articleService.deleteArticleFromGridFS(articleId);
        // 从数据库中删除文章
        articleService.deleteArticle(userId, articleId);
        // 从Tomcat容器中删除文章HTML静态页面
        // deleteArticleHTML(articleId);

        // 发布消息，让article-html服务删除文章HTML静态页面
        publishMessageDeletingHTML(articleId);
        return GraceJSONResult.ok();
    }

    @Deprecated
    private void deleteArticleHTML(String articleId) {
        String deleteUrl = "http://html.imoocnews.com:8002/article/html/delete?articleId=" + articleId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(deleteUrl, Integer.class);
        int status = responseEntity.getBody();
        if (status != HttpStatus.OK.value()) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }
    }

    private void publishMessageDeletingHTML(String articleId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.ARTICLE_EXCHANGE, "article.delete", articleId);
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.updateArticleStatus(articleId, ArticleReviewStatus.WITHDRAW.type);
        return GraceJSONResult.ok();
    }
}
