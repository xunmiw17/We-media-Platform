package com.xunmiw.api.controller.user;

import com.xunmiw.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@Api(value = "用户管理的相关接口定义", tags = {"用户管理的相关接口定义"})
@RequestMapping("appUser")
public interface AppUserMngControllerApi {

    @ApiOperation(value = "查询所有用户列表", notes = "查询所有用户列表", httpMethod = "POST")
    @PostMapping("/queryAll")
    public GraceJSONResult queryAll(@RequestParam String nickname,
                                    @RequestParam Integer status,
                                    @RequestParam Date startDate,
                                    @RequestParam Date endDate,
                                    @RequestParam Integer page,
                                    @RequestParam Integer pageSize);
}
