package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.UserControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.bo.UpdateUserInfoBO;
import com.xunmiw.pojo.vo.AppUserVO;
import com.xunmiw.pojo.vo.UserAccountInfoVO;
import com.xunmiw.user.service.UserService;
import com.xunmiw.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController extends BaseController implements UserControllerApi {

    final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        // 0. 判断参数不能为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. 根据userId查询用户信息
        AppUser appUser = getUser(userId);

        // 2. 将AppUser包装成展示给用户的基本信息VO对象
        AppUserVO appUserVO = new AppUserVO();
        BeanUtils.copyProperties(appUser, appUserVO);

        // 3. 返回用户基本信息
        return GraceJSONResult.ok(appUserVO);
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId) {
        // 0. 判断参数不能为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. 根据userId查询用户信息
        AppUser appUser = getUser(userId);

        // 2. 将AppUser包装成展示给用户的账户信息VO对象
        UserAccountInfoVO userAccountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(appUser, userAccountInfoVO);

        // 3. 返回用户账户信息
        return GraceJSONResult.ok(userAccountInfoVO);
    }

    @Override
    public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO, BindingResult result) {
        // 0. 校验BO
        if (result.hasErrors()) {
            Map<String, String> map = getErrors(result);
            return GraceJSONResult.errorMap(map);
        }

        // 1. 执行更新操作
        userService.updateUserInfo(updateUserInfoBO);
        return GraceJSONResult.ok();
    }

    /**
     * 由于用户信息不怎么变动，对于千万级别并发量的网站来说，可以缓存用户信息到Redis，减少数据库压力
     * @param userId
     * @return
     */
    private AppUser getUser(String userId) {
        // 查询判断Redis中是否已经包含用户信息，如包含则不再需要访问数据库
        String userJson = redisOperator.get(REDIS_USER_INFO + ":" + userId);
        AppUser appUser = null;
        if (StringUtils.isNotBlank(userJson)) {
            appUser = JsonUtils.jsonToPojo(userJson, AppUser.class);
        } else {
            appUser = userService.getUser(userId);

            // 将第一次查询到的数据存入Redis
            redisOperator.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(appUser));
        }

        return appUser;
    }
}