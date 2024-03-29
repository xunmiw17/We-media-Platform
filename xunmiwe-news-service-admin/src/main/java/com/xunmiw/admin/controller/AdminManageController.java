package com.xunmiw.admin.controller;

import com.xunmiw.admin.service.AdminUserService;
import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.admin.AdminManageControllerApi;
import com.xunmiw.enums.FaceVerifyType;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.AdminUser;
import com.xunmiw.pojo.bo.AdminLoginBO;
import com.xunmiw.pojo.bo.NewAdminBO;
import com.xunmiw.utils.FaceVerifyUtils;
import com.xunmiw.utils.PagedGridResult;
import com.xunmiw.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@RestController
public class AdminManageController extends BaseController implements AdminManageControllerApi {

    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FaceVerifyUtils faceVerifyUtils;

    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        // 1. 查询admin用户信息
        AdminUser adminUser = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());

        // 2. 判断admin不为空，如果为空则登录失败
        if (adminUser == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }

        // 3. 判断密码是否匹配
        boolean isPwdMatch = BCrypt.checkpw(adminLoginBO.getPassword(), adminUser.getPassword());
        if (isPwdMatch) {
            doLoginSettings(adminUser, request, response);
            return GraceJSONResult.ok();
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
    }

    @Override
    public GraceJSONResult adminIsExist(String username) {
        checkAdminExist(username);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        // 1. Base64不为空，则代表人脸入库，否则需要用户输入密码和确认密码
        if (StringUtils.isBlank(newAdminBO.getImg64())) {
            if (StringUtils.isBlank(newAdminBO.getPassword()) ||
                StringUtils.isBlank(newAdminBO.getConfirmPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
            }
        }

        // 2. 密码不为空，则判断两次输入必须一致
        if (StringUtils.isNotBlank(newAdminBO.getPassword())) {
            if (!newAdminBO.getPassword().equals(newAdminBO.getConfirmPassword())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }
        }

        // 3. 校验用户名唯一（尽管前端在输入框会调用Event提醒，但用户仍可以传入存在的用户名）
        checkAdminExist(newAdminBO.getUsername());

        // 4. 调用service，存入admin信息
        adminUserService.createAdminUser(newAdminBO);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        // 将分页结果（当前页，总页数，总记录数，内容）返回给前端
        PagedGridResult pagedGridResult = adminUserService.queryAdminList(page, pageSize);
        return GraceJSONResult.ok(pagedGridResult);
    }

    @Override
    public GraceJSONResult adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        // 从Redis中删除admin的会话token
        redisOperator.del(REDIS_ADMIN_TOKEN + ":" + adminId);

        // 从Cookie中清理admin登录的相关信息
        deleteCookie(request, response, "atoken");
        deleteCookie(request, response, "aid");
        deleteCookie(request, response, "aname");

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        // 0. 判断用户名和人脸信息不能为空
        if (StringUtils.isBlank(adminLoginBO.getUsername())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        String base64Client = adminLoginBO.getImg64();
        if (StringUtils.isBlank(base64Client)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_NULL_ERROR);
        }

        // 1. 从数据库中查询faceId
        AdminUser adminUser = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        String faceId = adminUser.getFaceId();
        if (StringUtils.isBlank(faceId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 2. 请求文件服务，获得人脸数据的base64数据
        String readFace64Url = "http://files.imoocnews.com:8004/fs/readFace64InGridFS?faceId=" + faceId;
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(readFace64Url, GraceJSONResult.class);
        GraceJSONResult body = responseEntity.getBody();
        String base64DB = (String) body.getData();

        // 3. 调用阿里AI进行人脸对比识别，判断可信度，实现人脸登录
        boolean result = faceVerifyUtils.faceVerify(FaceVerifyType.BASE64.type, base64Client, base64DB, 60);
        if (!result) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 4. admin登录后的数据设置，Redis与Cookie
        doLoginSettings(adminUser, request, response);
        return GraceJSONResult.ok();
    }

    /**
     * 用于admin用户过后的基本信息设置
     * @param adminUser
     * @param request
     * @param response
     */
    private void doLoginSettings(AdminUser adminUser,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // 保存token，放入Redis中
        String token = UUID.randomUUID().toString();
        redisOperator.set(REDIS_ADMIN_TOKEN + ":" + adminUser.getId(), token);

        // 保存admin登录基本token、id、username信息到Cookie中
        setCookie(request, response, "atoken", token, COOKIE_MONTH);
        setCookie(request, response, "aid", adminUser.getId(), COOKIE_MONTH);
        setCookie(request, response, "aname", adminUser.getAdminName(), COOKIE_MONTH);
    }

    private void checkAdminExist(String username) {
        AdminUser adminUser = adminUserService.queryAdminByUsername(username);
        if (adminUser != null) {
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }
}
