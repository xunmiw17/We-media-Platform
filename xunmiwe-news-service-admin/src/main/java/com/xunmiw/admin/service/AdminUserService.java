package com.xunmiw.admin.service;

import com.xunmiw.pojo.AdminUser;

public interface AdminUserService {

    /**
     * 获得管理员的用户信息
     * @param username
     * @return
     */
    public AdminUser queryAdminByUsername(String username);
}
