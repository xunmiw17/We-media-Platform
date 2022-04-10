package com.xunmiw.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.enums.UserStatus;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.user.mapper.AppUserMapper;
import com.xunmiw.user.service.AppUserMngService;
import com.xunmiw.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class AppUserMngServiceImpl extends BaseService implements AppUserMngService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Override
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {
        Example example = new Example(AppUser.class);
        example.orderBy("createdTime").desc();
        Example.Criteria criteria = example.createCriteria();

        if (StringUtils.isNotBlank(nickname)) {
            criteria.andLike("nickname", "%" + nickname + "%");
        }
        if (UserStatus.isUserStatusValid(status)) {
            criteria.andEqualTo("activeStatus", status);
        }
        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("createdTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("createdTime", endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<AppUser> appUsers = appUserMapper.selectByExample(example);
        return setPagedGrid(appUsers, page);
    }
}
