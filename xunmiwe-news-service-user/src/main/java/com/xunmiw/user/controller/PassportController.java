package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.PassportControllerApi;
import com.xunmiw.enums.UserStatus;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.bo.LoginBO;
import com.xunmiw.user.service.UserService;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class PassportController extends BaseController implements PassportControllerApi {

    final static Logger logger = LoggerFactory.getLogger(PassportController.class);

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request) {
        try {
            // 获取用户IP
            String userIP = IPUtil.getRequestIp(request);

            // 根据用户ip进行限制，60秒内只能获取一次验证码
            redisOperator.setnx60s(MOBILE_SMSCODE + ":" + userIP, userIP);

            // 生成随机验证码并发送短信
            String randomCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
            // smsUtils.sendSMS("13927420560", randomCode);

            // 把验证码存入redis，用于后续验证，并设置其失效时间为30分钟，以防止验证码永久存入Redis占用内存
            redisOperator.set(MOBILE_SMSCODE + ":" + mobile, randomCode, 30 * 60);
            return GraceJSONResult.ok();
        } catch (Exception e) {
            return GraceJSONResult.error();
        }
    }

    @Override
    public GraceJSONResult doLogin(LoginBO loginBO, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验验证码
        String mobile = loginBO.getMobile();
        String smsCode = loginBO.getSmsCode();
        String redisSMS = redisOperator.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisSMS) || !smsCode.equals(redisSMS)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 查询数据库，判断该用户是否注册
        AppUser appUser = userService.mobileExists(mobile);
        // 如用户存在，并且为冻结状态，则抛出异常，禁止登录
        if (appUser != null && appUser.getActiveStatus() == UserStatus.FROZEN.type) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        } else if (appUser == null) {
            appUser = userService.createUser(mobile);
        }

        // 3. 保存用户分布式会话的相关操作
        int activeStatus = appUser.getActiveStatus();
        if (activeStatus != UserStatus.FROZEN.type) {
            // 保存随机生成的token到Redis
            String token = UUID.randomUUID().toString();
            redisOperator.set(REDIS_USER_TOKEN + ":" + appUser.getId(), token);
            redisOperator.set(REDIS_USER_INFO + ":" + appUser.getId(), JsonUtils.objectToJson(appUser));

            // 保存用户id和token到cookie中
            setCookie(request, response, "utoken", token, COOKIE_MONTH);
            setCookie(request, response, "uid", appUser.getId(), COOKIE_MONTH);
        }

        // 4. 用户登录/注册成功后，需要删除Redis中存储的短信验证码，验证码只能使用一次
        redisOperator.del(MOBILE_SMSCODE + ":" + mobile);

        // 5. 返回用户状态
        return GraceJSONResult.ok(activeStatus);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {
        // 只需要在Redis中删除token，没有必要删除user info
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);
        setCookie(request, response, "utoken", "", COOKIE_DELETE);
        setCookie(request, response, "uid", "", COOKIE_DELETE);

        return GraceJSONResult.ok();
    }
}
