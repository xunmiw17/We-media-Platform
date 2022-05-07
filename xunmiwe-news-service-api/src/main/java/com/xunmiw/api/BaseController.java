package com.xunmiw.api;

import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseController {

    @Autowired
    public RedisOperator redisOperator;

    @Autowired
    public RestTemplate restTemplate;

    // 注入服务发现，获得已经注册的服务相关信息
    @Autowired
    private DiscoveryClient discoveryClient;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_CATEGORY_LIST = "redis_category_list";
    public static final String REDIS_WRITER_FANS_COUNT = "redis_writer_fans_count";
    public static final String REDIS_USER_FOLLOW_COUNT = "redis_user_follow_count";
    public static final String REDIS_ARTICLE_READ_COUNT = "redis_article_read_count";
    public static final String REDIS_ARTICLE_ALREADY_READ = "redis_article_already_read";
    public static final String REDIS_ARTICLE_COMMENT_COUNT = "redis_article_comment_count";

    public static final Integer COOKIE_MONTH = 30 * 24 * 60 * 60;
    public static final Integer COOKIE_DELETE = 0;

    public static final Integer DEFAULT_START_PAGE = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    @Value("${website.domain-name}")
    public String DOMAIN_NAME;

    /**
     * 获取BO中的错误信息
     * @param result
     */
    public Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError fieldError : errorList) {
            // 发生验证错误时对应的属性
            String field = fieldError.getField();
            // 验证的错误消息
            String msg = fieldError.getDefaultMessage();
            map.put(field, msg);
        }
        return map;
    }

    public void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue,
                            Integer maxAge) {
        try {
            cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
            setCookieValue(request, response, cookieName, cookieValue, maxAge);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue,
                                 Integer maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        // cookie.setDomain("imoocnews.com");
        cookie.setDomain(DOMAIN_NAME);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request,
                             HttpServletResponse response,
                             String cookieName) {
        try {
            // Encode空字符串，避免引起异常
            String deleteValue = URLEncoder.encode("", "utf-8");
            setCookieValue(request, response, cookieName, deleteValue, COOKIE_DELETE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Category checkCategory(String jsonCategories, Integer catId) {
        List<Category> categories = JsonUtils.jsonToList(jsonCategories, Category.class);
        for (Category category : categories) {
            if (category.getId() == catId) {
                return category;
            }
        }
        return null;
    }

    /**
     * 发起远程调用，获得指定用户(们)的基本信息
     * @param userIds
     * @return
     */
    public List<AppUserVO> getUserList(Set<String> userIds) {
        String serviceId = "SERVICE-USER";
        // 根据serviceId实现服务发现，提供了负载均衡功能
        // List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        // ServiceInstance userServiceInstance = instances.get(0);

        // 直接通过serviceId得到服务URL地址
        String getUserInfoUrl = "http://" + serviceId + "/user/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);

        // 通过Eureka服务发现动态获取服务地址
        //String getUserInfoUrl = "http://" + userServiceInstance.getHost() + ":" + userServiceInstance.getPort() + "/user"
        //        + "/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);

        // 硬编码服务地址
        //  String getUserInfoUrl = "http://user.imoocnews.com:8003/user/queryUserByIds?userIds=" + JsonUtils.objectToJson(userIds);
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(getUserInfoUrl, GraceJSONResult.class);
        GraceJSONResult responseBody = responseEntity.getBody();

        // 注意这里使用equals方法进行两个Integer的比较，如果使用==，即使Integer的值相同，但Integer的地址不同，比较结果仍然为false
        if (!responseBody.getStatus().equals(ResponseStatusEnum.SUCCESS.status())) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }
        String usersJson = JsonUtils.objectToJson(responseBody.getData());
        List<AppUserVO> users = JsonUtils.jsonToList(usersJson, AppUserVO.class);
        return users;
    }
}
