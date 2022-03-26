package com.xunmiw.user.service.impl;

import com.xunmiw.enums.Sex;
import com.xunmiw.enums.UserStatus;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.bo.UpdateUserInfoBO;
import com.xunmiw.user.mapper.AppUserMapper;
import com.xunmiw.user.service.UserService;
import com.xunmiw.utils.DateUtil;
import com.xunmiw.utils.DesensitizationUtil;
import com.xunmiw.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private Sid sid;

    @Autowired
    public RedisOperator redisOperator;

    public static final String REDIS_USER_INFO = "redis_user_info";

    private static final String USER_FACE0 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";
    private static final String USER_FACE2 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUx6ANoEMAABTntpyjOo395.png";


    @Override
    public AppUser mobileExists(String mobile) {
        Example userExample = new Example(AppUser.class);
        Example.Criteria userCriteria = userExample.createCriteria();
        userCriteria.andEqualTo("mobile", mobile);
        AppUser appUser = appUserMapper.selectOneByExample(userExample);
        return appUser;
    }

    @Transactional
    @Override
    public AppUser createUser(String mobile) {
        // 如业务激增，则需要分库分表，那么userId就要保证全局唯一
        // 这里使用Sid工具类生成全局唯一的userId
        String userId = sid.nextShort();
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setMobile(mobile);
        appUser.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        appUser.setFace(USER_FACE0);
        appUser.setBirthday(DateUtil.stringToDate("1900-01-01"));
        appUser.setSex(Sex.secret.type);
        appUser.setActiveStatus(UserStatus.INACTIVE.type);
        appUser.setTotalIncome(0);
        appUser.setCreatedTime(new Date());
        appUser.setUpdatedTime(new Date());
        appUserMapper.insert(appUser);
        return appUser;
    }

    @Override
    public AppUser getUser(String userId) {
        return appUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {

        String userId = updateUserInfoBO.getId();

        // 为保证双写一致性，先删除Redis中的数据，再更新数据库
        redisOperator.del(REDIS_USER_INFO + ":" + userId);

        AppUser appUser = new AppUser();
        BeanUtils.copyProperties(updateUserInfoBO, appUser);

        appUser.setActiveStatus(UserStatus.ACTIVE.type);
        appUser.setUpdatedTime(new Date());

        int result = appUserMapper.updateByPrimaryKeySelective(appUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        // 缓存双删策略
        try {
            Thread.sleep(100);
            redisOperator.del(REDIS_USER_INFO + ":" + userId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
