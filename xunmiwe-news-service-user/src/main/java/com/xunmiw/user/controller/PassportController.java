package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.PassportControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.SMSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class PassportController extends BaseController implements PassportControllerApi {

    final static Logger logger = LoggerFactory.getLogger(PassportController.class);

    @Autowired
    private SMSUtils smsUtils;

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
}
