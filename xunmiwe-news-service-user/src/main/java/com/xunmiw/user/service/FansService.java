package com.xunmiw.user.service;

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
}
