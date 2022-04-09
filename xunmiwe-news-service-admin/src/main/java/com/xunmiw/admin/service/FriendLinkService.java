package com.xunmiw.admin.service;

import com.xunmiw.pojo.mo.FriendLinkMO;

import java.util.List;

public interface FriendLinkService {

    /**
     * 新增或更新友情链接
     * @param friendLinkMO
     */
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO);

    /**
     * 查询友情链接
     * @return
     */
    public List<FriendLinkMO> queryAllFriendLinkList();

    /**
     * 删除友情链接
     */
    public void deleteFriendLink(String linkId);
}
