package com.xunmiw.api.controller.user;

import com.xunmiw.api.config.ServiceList;
import com.xunmiw.api.controller.user.fallbacks.UserControllerFallbackFactory;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.UpdateUserInfoBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "用户信息相关Controller", tags = {"用户信息相关Controller"})
@RequestMapping("user")
@FeignClient(value = ServiceList.SERVICE_USER, fallbackFactory = UserControllerFallbackFactory.class)
public interface UserControllerApi {

    @ApiOperation(value = "获得用户基本信息", notes = "获得用户基本信息", httpMethod = "POST")
    @PostMapping("getUserInfo")
    public GraceJSONResult getUserInfo(@RequestParam String userId);

    @ApiOperation(value = "获得用户账户信息", notes = "获得用户账户信息", httpMethod = "POST")
    @PostMapping("getAccountInfo")
    public GraceJSONResult getAccountInfo(@RequestParam String userId);

    @ApiOperation(value = "完善用户信息", notes = "完善用户信息", httpMethod = "POST")
    @PostMapping("updateUserInfo")
    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserInfoBO updateUserInfoBO);

    @ApiOperation(value = "根据用户的ids查询用户列表", notes = "根据用户的ids查询用户列表", httpMethod = "GET")
    @GetMapping("queryUserByIds")
    public GraceJSONResult queryUserByIds(@RequestParam String userIds);
}
