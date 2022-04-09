package com.xunmiw.api.controller.admin;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.FriendLinkBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Api(value = "首页友情链接维护", tags = {"首页友情链接维护的controller"})
@RequestMapping("friendLinkMng")
public interface FriendLinkControllerApi {

    @ApiOperation(value = "新增/修改友情链接", notes = "新增/修改友情链接", httpMethod = "POST")
    @PostMapping("/saveOrUpdateFriendLink")
    public GraceJSONResult saveOrUpdateFriendLink(@RequestBody @Valid FriendLinkBO friendLinkBO,
                                                  BindingResult bindingResult);

    @ApiOperation(value = "查询友情链接列表", notes = "查询友情链接列表", httpMethod = "POST")
    @PostMapping("getFriendLinkList")
    public GraceJSONResult getFriendLinkList();

    @ApiOperation(value = "删除友情链接", notes = "删除友情链接", httpMethod = "POST")
    @PostMapping("delete")
    public GraceJSONResult deleteFriendList(@RequestParam String linkId);
}
