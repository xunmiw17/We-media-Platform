package com.xunmiw.api;

import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseController {

    @Autowired
    public RedisOperator redisOperator;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";

    public static final Integer COOKIE_MONTH = 30 * 24 * 60 * 60;

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

    public void setCookieValue(HttpServletRequest request, HttpServletResponse response, String cookieName, String cookieValue,
                                 Integer maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        // cookie.setDomain("imoocnews.com");
        cookie.setDomain(DOMAIN_NAME);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
