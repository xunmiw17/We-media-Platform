package com.xunmiw.admin.service.impl;

import com.xunmiw.admin.mapper.AdminUserMapper;
import com.xunmiw.admin.service.AdminUserService;
import com.xunmiw.pojo.AdminUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public AdminUser queryAdminByUsername(String username) {
        Example adminExample = new Example(AdminUser.class);
        Example.Criteria criteria = adminExample.createCriteria();
        criteria.andEqualTo("username", username);
        AdminUser admin = adminUserMapper.selectOneByExample(adminExample);
        return admin;
    }
}
