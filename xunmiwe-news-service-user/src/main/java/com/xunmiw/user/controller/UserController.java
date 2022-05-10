package com.xunmiw.user.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@DefaultProperties(defaultFallback = "defaultFallback")
public class UserController extends BaseController implements UserControllerApi {

    final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Value("${server.port}")
    private String currPort;

    public GraceJSONResult defaultFallback() {
        System.out.println("全局降级");
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_GLOBAL);
    }

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        // 0. 判断参数不能为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. 根据userId查询用户信息，并将AppUser包装成展示给用户的基本信息VO对象
        AppUserVO appUserVO = getBasicUserInfo(userId);

        // 2. 返回用户基本信息
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
    public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
        // 1. 执行更新操作
        userService.updateUserInfo(updateUserInfoBO);
        return GraceJSONResult.ok();
    }

    @Override
    @HystrixCommand//(fallbackMethod = "queryUserByIdsFallback")
    public GraceJSONResult queryUserByIds(String userIds) {

        // int a = 1 / 0;
        
        System.out.println("====================================================" + currPort);
        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        List<AppUserVO> publishers = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);
        for (String userId : userIdList) {
            AppUserVO appUserVO = getBasicUserInfo(userId);
            publishers.add(appUserVO);
        }
        return GraceJSONResult.ok(publishers);
    }

    /**
     * Fallback Method，构建空对象并返回
     * @param userIds
     * @return
     */
    public GraceJSONResult queryUserByIdsFallback(String userIds) {

        System.out.println("进入降级方法==================================================");
        System.out.println("====================================================" + currPort);
        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        List<AppUserVO> publishers = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds, String.class);
        for (String userId : userIdList) {
            // 构建空对象，返回
            AppUserVO appUserVO = new AppUserVO();
            publishers.add(appUserVO);
        }
        return GraceJSONResult.ok(publishers);
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

    private AppUserVO getBasicUserInfo(String userId) {
        // 1. 根据userId查询用户信息
        AppUser appUser = getUser(userId);

        // 2. 将AppUser包装成展示给用户的基本信息VO对象
        AppUserVO appUserVO = new AppUserVO();
        BeanUtils.copyProperties(appUser, appUserVO);

        // 3. 查询Redis中用户关注数与粉丝数，放入AppUserVO中
        String followCountStr = redisOperator.get(REDIS_USER_FOLLOW_COUNT + ":" + userId);
        String fansCountStr = redisOperator.get(REDIS_WRITER_FANS_COUNT + ":" + userId);
        if (StringUtils.isNotBlank(followCountStr))
            appUserVO.setMyFollowCounts(Integer.valueOf(followCountStr));
        if (StringUtils.isNotBlank(fansCountStr))
            appUserVO.setMyFansCounts(Integer.valueOf(fansCountStr));
        return appUserVO;
    }
}
