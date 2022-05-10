package com.xunmiw.api.controller.user.fallbacks;

import com.xunmiw.api.controller.user.UserControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.bo.UpdateUserInfoBO;
import com.xunmiw.pojo.vo.AppUserVO;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserControllerFallbackFactory implements FallbackFactory<UserControllerApi> {

    @Override
    public UserControllerApi create(Throwable throwable) {
        return new UserControllerApi() {
            @Override
            public GraceJSONResult getUserInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
            }

            @Override
            public GraceJSONResult getAccountInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
            }

            @Override
            public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_FEIGN);
            }

            @Override
            public GraceJSONResult queryUserByIds(String userIds) {
                System.out.println("进入客户端的降级方法");
                List<AppUserVO> publishers = new ArrayList<>();
                return GraceJSONResult.ok(publishers);
            }
        };
    }
}
