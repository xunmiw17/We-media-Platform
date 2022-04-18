package com.xunmiw.user.service.impl;

import com.xunmiw.api.service.BaseService;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.Fans;
import com.xunmiw.user.mapper.FansMapper;
import com.xunmiw.user.service.FansService;
import com.xunmiw.user.service.UserService;
import com.xunmiw.utils.RedisOperator;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FansServiceImpl extends BaseService implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @Autowired
    private RedisOperator redisOperator;

    @Override
    public boolean isMeFollowThisWriter(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);
        int count = fansMapper.selectCount(fans);
        return count > 0;
    }

    @Override
    @Transactional
    public void follow(String writerId, String fanId) {
        AppUser appUser = userService.getUser(fanId);

        Fans fans = new Fans();
        fans.setId(sid.nextShort());
        fans.setFanId(fanId);
        fans.setWriterId(writerId);

        fans.setFace(appUser.getFace());
        fans.setSex(appUser.getSex());
        fans.setFanNickname(appUser.getNickname());
        fans.setProvince(appUser.getProvince());

        int result = fansMapper.insert(fans);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // Redis作家粉丝数 & 当前用户关注数累加
        redisOperator.increment(REDIS_WRITER_FANS_COUNT + ":" + writerId, 1);
        redisOperator.increment(REDIS_USER_FOLLOW_COUNT + ":" + fanId, 1);
    }

    @Override
    @Transactional
    public void unfollow(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);
        int result = fansMapper.delete(fans);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_ERROR);
        }

        // Redis作家粉丝数 & 当前用户关注数累减
        redisOperator.decrement(REDIS_WRITER_FANS_COUNT + ":" + writerId, 1);
        redisOperator.decrement(REDIS_USER_FOLLOW_COUNT + ":" + fanId, 1);
    }
}
