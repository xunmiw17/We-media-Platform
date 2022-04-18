package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.FansControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.user.service.FansService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

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
}
