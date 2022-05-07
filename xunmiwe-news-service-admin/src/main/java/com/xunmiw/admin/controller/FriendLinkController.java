package com.xunmiw.admin.controller;

import com.xunmiw.admin.service.FriendLinkService;
import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.admin.FriendLinkControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.FriendLinkBO;
import com.xunmiw.pojo.mo.FriendLinkMO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class FriendLinkController extends BaseController implements FriendLinkControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FriendLinkController.class);

    @Autowired
    private FriendLinkService friendLinkService;

    @Override
    public GraceJSONResult saveOrUpdateFriendLink(FriendLinkBO friendLinkBO) {
        FriendLinkMO friendLinkMO = new FriendLinkMO();
        BeanUtils.copyProperties(friendLinkBO, friendLinkMO);
        friendLinkMO.setCreateTime(new Date());
        friendLinkMO.setUpdateTime(new Date());

        friendLinkService.saveOrUpdateFriendLink(friendLinkMO);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getFriendLinkList() {
        List<FriendLinkMO> friendLinkList = friendLinkService.queryAllFriendLinkList();
        return GraceJSONResult.ok(friendLinkList);
    }

    @Override
    public GraceJSONResult deleteFriendList(String linkId) {
        friendLinkService.deleteFriendLink(linkId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryUserPortalFriendLinkList() {
        return GraceJSONResult.ok(friendLinkService.queryUserPortalFriendLinkList());
    }
}
