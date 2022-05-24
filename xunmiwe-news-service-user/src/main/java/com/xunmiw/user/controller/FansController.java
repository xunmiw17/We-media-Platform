package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.FansControllerApi;
import com.xunmiw.enums.Sex;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.vo.FansCountVO;
import com.xunmiw.pojo.vo.RegionRatioVO;
import com.xunmiw.user.service.FansService;
import com.xunmiw.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FansController extends BaseController implements FansControllerApi {

    @Autowired
    private FansService fansService;

    @Override
    public GraceJSONResult isMeFollowThisWriter(String writerId, String fanId) {
        boolean follow = fansService.isMeFollowThisWriter(writerId, fanId);
        return GraceJSONResult.ok(follow);
    }

    @Override
    public GraceJSONResult follow(String writerId, String fanId) {
        fansService.follow(writerId, fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult unfollow(String writerId, String fanId) {
        fansService.unfollow(writerId, fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryAll(String writerId, Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        // 从Elasticsearch中获取粉丝数据
        PagedGridResult result = fansService.queryAllFromES(writerId, page, pageSize);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult queryRatio(String writerId) {
        Integer manCount = fansService.queryFansRatioAndCounts(writerId, Sex.man);
        Integer womanCount = fansService.queryFansRatioAndCounts(writerId, Sex.woman);
        FansCountVO fansCountVO = new FansCountVO();
        fansCountVO.setManCounts(manCount);
        fansCountVO.setWomanCounts(womanCount);
        return GraceJSONResult.ok(fansCountVO);
    }

    @Override
    public GraceJSONResult queryRatioByRegion(String writerId) {
        List<RegionRatioVO> result = fansService.queryFansRatioAndCountsByRegion(writerId);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult forceUpdateFanInfo(String relationId, String fanId) {
        fansService.forceUpdateFanInfo(relationId, fanId);
        return GraceJSONResult.ok();
    }
}
