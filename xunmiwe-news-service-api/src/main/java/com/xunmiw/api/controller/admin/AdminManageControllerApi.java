package com.xunmiw.api.controller.admin;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.AdminLoginBO;
import com.xunmiw.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "管理员admin维护", tags = {"管理员admin维护的controller"})
@RequestMapping("adminMng")
public interface AdminManageControllerApi {

    @ApiOperation(value = "admin登录", notes = "admin登录", httpMethod = "POST")
    @PostMapping("/adminLogin")
    public GraceJSONResult adminLogin(@RequestBody @Valid AdminLoginBO adminLoginBO,
                                      HttpServletRequest request,
                                      HttpServletResponse response);

    @ApiOperation(value = "根据用户名查看admin是否存在", notes = "根据用户名查看admin是否存在", httpMethod = "POST")
    @PostMapping("/adminIsExist")
    public GraceJSONResult adminIsExist(@RequestParam String username);

    @ApiOperation(value = "创建admin", notes = "创建admin", httpMethod = "POST")
    @PostMapping("/addNewAdmin")
    public GraceJSONResult addNewAdmin(@RequestBody NewAdminBO newAdminBO,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @ApiOperation(value = "分页查询admin列表", notes = "分页查询admin列表", httpMethod = "POST")
    @PostMapping("/getAdminList")
    public GraceJSONResult getAdminList(
            @RequestParam @ApiParam(name = "page", value = "查询页数", required = false) Integer page,
            @RequestParam @ApiParam(name = "pageSize", value = "每页条数", required = false) Integer pageSize
    );

    @ApiOperation(value = "admin退出登录", notes = "admin退出登录", httpMethod = "POST")
    @PostMapping("/adminLogout")
    public GraceJSONResult adminLogout(@RequestParam String adminId,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @ApiOperation(value = "admin管理员的人脸登录", notes = "admin管理员的人脸登录", httpMethod = "POST")
    @PostMapping("/adminFaceLogin")
    public GraceJSONResult adminFaceLogin(@RequestBody AdminLoginBO adminLoginBO,
                                          HttpServletRequest request,
                                          HttpServletResponse response);
}
