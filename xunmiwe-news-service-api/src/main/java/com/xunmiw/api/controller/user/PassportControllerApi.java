package com.xunmiw.api.controller.user;

import com.xunmiw.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

// @Api注解代表此类会被Swagger2找到
@Api(value = "用户注册登录", tags = {"用户注册登录的controller"})
@RequestMapping("passport")
public interface PassportControllerApi {
    /**
     * HelloController的所有接口都在此暴露，所有实现都在各自的微服务中
     *
     * 其次，微服务之间的调用都是基于接口的，如果不这么做，微服务之间的调用就需要相互依赖，耦合度就会变高，接口能够做到解耦
     *
     * Swagger2，基于接口的自动文档生成
     */

    @ApiOperation(value = "获得短信验证码", notes = "获得短信验证码", httpMethod = "GET")
    @GetMapping("/getSMSCode")
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request);
}
