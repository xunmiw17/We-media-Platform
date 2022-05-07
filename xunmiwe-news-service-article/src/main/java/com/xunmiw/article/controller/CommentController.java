package com.xunmiw.article.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.article.CommentControllerApi;
import com.xunmiw.api.controller.user.UserControllerApi;
import com.xunmiw.article.service.CommentService;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.bo.CommentReplyBO;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class CommentController extends BaseController implements CommentControllerApi {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserControllerApi userControllerApi;

    @Override
    public GraceJSONResult createComment(CommentReplyBO commentReplyBO) {

        // 1. 根据留言用户Id查询用户信息，用于放入Comment表中做冗余处理，避免查询Comment时进行多表关联
        String userId = commentReplyBO.getCommentUserId();

        // 2. 发起远程调用，获得用户nickname
        Set<String> ids = new HashSet<>();
        ids.add(userId);
        List<AppUserVO> users = getUserList(ids);
        AppUserVO appUserVO = users.get(0);
        String nickname = appUserVO.getNickname();
        String commentUserFace = appUserVO.getFace();

        // 3. 保存用户评论信息到数据库
        commentService.createComment(commentReplyBO.getArticleId(), commentReplyBO.getFatherId(),
                                            commentReplyBO.getContent(), userId, nickname, commentUserFace);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult counts(String articleId) {
        String commentCountStr = redisOperator.get(REDIS_ARTICLE_COMMENT_COUNT + ":" + articleId);
        Integer count = 0;
        if (StringUtils.isNotBlank(commentCountStr))
            count = Integer.valueOf(commentCountStr);
        return GraceJSONResult.ok(count);
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = commentService.listComments(articleId, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult result = commentService.mng(writerId, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        commentService.delete(commentId, writerId);
        return GraceJSONResult.ok();
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

        // 注意这里使用equals方法进行两个Integer的比较，如果使用==，即使Integer的值相同，但Integer的地址不同，比较结果仍然为false
        if (!responseBody.getStatus().equals(ResponseStatusEnum.SUCCESS.status())) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }
        String usersJson = JsonUtils.objectToJson(responseBody.getData());
        List<AppUserVO> users = JsonUtils.jsonToList(usersJson, AppUserVO.class);
        return users;
    }
}
