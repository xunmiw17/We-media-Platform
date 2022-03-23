package com.imooc.api.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

// @Api注解代表此类会被Swagger2找到
@Api(value = "controller的标题", tags = {"xx功能的controller"})
public interface HelloControllerApi {
    /**
     * HelloController的所有接口都在此暴露，所有实现都在各自的微服务中
     *
     * 其次，微服务之间的调用都是基于接口的，如果不这么做，微服务之间的调用就需要相互依赖，耦合度就会变高，接口能够做到解耦
     *
     * Swagger2，基于接口的自动文档生成
     */

    @ApiOperation(value = "hello方法的接口", notes = "hello方法的接口", httpMethod = "GET")
    @GetMapping("/hello")
    public Object hello();
}
