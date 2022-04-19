package com.xunmiw.user.service;

import com.xunmiw.pojo.Fans;
import com.xunmiw.utils.PagedGridResult;

import java.util.List;

public interface FansService {

    /**
     * 关注作家
     * @param writerId
     * @param fanId
     */
    public void follow(String writerId, String fanId);

    /**
     * 查询是否关注了该作家
     * @param writerId
     * @param fanId
     * @return
     */
    public boolean isMeFollowThisWriter(String writerId, String fanId);

    /**
     * 取关作家
     * @param writerId
     * @param fanId
     */
    public void unfollow(String writerId, String fanId);

    /**
     * 查询粉丝列表
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAll(String writerId, Integer page, Integer pageSize);
}
