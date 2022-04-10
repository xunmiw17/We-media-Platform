package com.xunmiw.user.controller;

import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.user.AppUserMngControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.user.service.AppUserMngService;
import com.xunmiw.utils.PagedGridResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class AppUserMngController extends BaseController implements AppUserMngControllerApi {

    final static Logger logger = LoggerFactory.getLogger(AppUserMngController.class);

    @Autowired
    private AppUserMngService appUserMngService;

    @Override
    public GraceJSONResult queryAll(String nickname, Integer status, Date startDate, Date endDate,
                                    Integer page, Integer pageSize) {
        if (page == null)
            page = DEFAULT_START_PAGE;
        if (pageSize == null)
            pageSize = DEFAULT_PAGE_SIZE;

        PagedGridResult pagedGridResult = appUserMngService.queryAllUserList(nickname, status, startDate, endDate, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }
}
