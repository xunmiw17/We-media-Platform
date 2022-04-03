package com.xunmiw.api.controller.admin;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.AdminLoginBO;
import com.xunmiw.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "管理员admin维护", tags = {"管理员admin维护的controller"})
@RequestMapping("adminMng")
public interface AdminManageControllerApi {

    @ApiOperation(value = "admin登录", notes = "admin登录", httpMethod = "POST")
    @PostMapping("/adminLogin")
    public GraceJSONResult adminLogin(@RequestBody @Valid AdminLoginBO adminLoginBO,
                                      BindingResult result,
                                      HttpServletRequest request,
                                      HttpServletResponse response);

    @ApiOperation(value = "根据用户名查看admin是否存在", notes = "根据用户名查看admin是否存在", httpMethod = "POST")
    @PostMapping("/adminIsExist")
    public GraceJSONResult adminIsExist(@RequestParam String username);

    @ApiOperation(value = "创建admin", notes = "创建admin", httpMethod = "POST")
    @PostMapping("/addNewAdmin")
    public GraceJSONResult addNewAdmin(@RequestBody NewAdminBO newAdminBO,
                                       BindingResult result,
                                       HttpServletRequest request,
                                       HttpServletResponse response);
}
