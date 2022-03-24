package com.xunmiw.user.controller;

import com.xunmiw.api.controller.user.UserControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.vo.UserAccountInfoVO;
import com.xunmiw.user.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserControllerApi {

    final static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getAccountInfo(String userId) {
        // 0. 判断参数不能为空
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 1. 根据userId查询用户信息
        AppUser appUser = getUser(userId);

        // 2. 将AppUser包装成展示给用户的VO对象
        UserAccountInfoVO userAccountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(appUser, userAccountInfoVO);

        // 3. 返回用户信息
        return GraceJSONResult.ok(userAccountInfoVO);
    }

    private AppUser getUser(String userId) {
        // TODO 本方法后续公用，并且扩展
        AppUser appUser = userService.getUser(userId);
        return appUser;
    }
}
