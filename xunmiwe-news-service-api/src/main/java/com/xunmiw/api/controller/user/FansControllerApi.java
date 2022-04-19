package com.xunmiw.api.controller.user;

import com.xunmiw.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(value = "粉丝管理的Controller", tags = {"粉丝管理的Controller"})
@RequestMapping("fans")
public interface FansControllerApi {

    @ApiOperation(value = "查询是否关注了该作家", notes = "查询是否关注了该作家", httpMethod = "POST")
    @PostMapping("isMeFollowThisWriter")
    public GraceJSONResult isMeFollowThisWriter(@RequestParam String writerId, @RequestParam String fanId);

    @ApiOperation(value = "关注作家", notes = "关注作家", httpMethod = "POST")
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String writerId, @RequestParam String fanId);

    @ApiOperation(value = "取关作家", notes = "取关作家", httpMethod = "POST")
    @PostMapping("unfollow")
    public GraceJSONResult unfollow(@RequestParam String writerId, @RequestParam String fanId);

    @ApiOperation(value = "查询粉丝列表", notes = "查询粉丝列表", httpMethod = "POST")
    @PostMapping("queryAll")
    public GraceJSONResult queryAll(@RequestParam String writerId,
                                    @RequestParam Integer page,
                                    @RequestParam Integer pageSize);
}
