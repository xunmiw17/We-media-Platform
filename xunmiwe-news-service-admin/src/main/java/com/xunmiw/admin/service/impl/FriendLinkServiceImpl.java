package com.xunmiw.admin.service.impl;

import com.xunmiw.admin.repository.FriendLinkRepository;
import com.xunmiw.admin.service.FriendLinkService;
import com.xunmiw.enums.YesOrNo;
import com.xunmiw.pojo.mo.FriendLinkMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendLinkServiceImpl implements FriendLinkService {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Override
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {
        friendLinkRepository.save(friendLinkMO);
    }

    @Override
    public List<FriendLinkMO> queryAllFriendLinkList() {
        List<FriendLinkMO> friendLinkList = friendLinkRepository.findAll();
        return friendLinkList;
    }

    @Override
    public void deleteFriendLink(String linkId) {
        friendLinkRepository.deleteById(linkId);
    }

    @Override
    public List<FriendLinkMO> queryUserPortalFriendLinkList() {
        return friendLinkRepository.getAllByIsDelete(YesOrNo.NO.type);
    }
}
