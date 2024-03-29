package com.xunmiw.api;

import com.xunmiw.pojo.Category;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
    public static final String REDIS_HOT_ARTICLE = "redis_hot_article";

    public static final Integer COOKIE_MONTH = 30 * 24 * 60 * 60;
    public static final Integer COOKIE_DELETE = 0;

    public static final Integer DEFAULT_START_PAGE = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    @Value("${website.domain-name}")
    public String DOMAIN_NAME;

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
}
