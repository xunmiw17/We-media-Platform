package com.xunmiw.user.service;

import com.xunmiw.pojo.AppUser;

public interface UserService {

    /**
     * 判断用户是否存在，如存在，返回AppUser信息
     * @param mobile
     * @return
     */
    public AppUser mobileExists(String mobile);

    /**
     * 创建用户，新增用户记录到数据库
     * @param mobile
     * @return
     */
    public AppUser createUser(String mobile);
}
