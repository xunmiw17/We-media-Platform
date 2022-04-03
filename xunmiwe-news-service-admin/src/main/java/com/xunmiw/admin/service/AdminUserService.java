package com.xunmiw.admin.service;

import com.xunmiw.pojo.AdminUser;
import com.xunmiw.pojo.bo.NewAdminBO;
import com.xunmiw.utils.PagedGridResult;

public interface AdminUserService {

    /**
     * 获得管理员的用户信息
     * @param username
     * @return
     */
    public AdminUser queryAdminByUsername(String username);

    /**
     * 新增管理员
     * @param newAdminBO
     */
    public void createAdminUser(NewAdminBO newAdminBO);

    /**
     * 分页查询admin列表
     * @param page
     * @param pageSize
     */
    public PagedGridResult queryAdminList(Integer page, Integer pageSize);
}
