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

    @ApiOperation(value = "查询男女粉丝数量/比例", notes = "查询男女粉丝数量/比例", httpMethod = "POST")
    @PostMapping("queryRatio")
    public GraceJSONResult queryRatio(@RequestParam String writerId);

    @ApiOperation(value = "根据地域查询粉丝数量/比例", notes = "根据地域查询粉丝数量/比例", httpMethod = "POST")
    @PostMapping("queryRatioByRegion")
    public GraceJSONResult queryRatioByRegion(@RequestParam String writerId);

    /**
     * 数据库的粉丝表由于存在用户表的冗余字段，当用户信息更新时不能保证强一致性，此方法为当用户点击到粉丝主页时实现的粉丝信息的更新
     * @param relationId
     * @param fanId
     * @return
     */
    @ApiOperation(value = "被动更新粉丝用户信息", notes = "被动更新粉丝用户信息", httpMethod = "POST")
    @PostMapping("forceUpdateFanInfo")
    public GraceJSONResult forceUpdateFanInfo(@RequestParam String relationId,
                                              @RequestParam String fanId);
}
