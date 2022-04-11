package com.xunmiw.user.service;

import com.xunmiw.pojo.AppUser;
import com.xunmiw.pojo.bo.UpdateUserInfoBO;
import com.xunmiw.utils.PagedGridResult;

import java.util.Date;

public interface AppUserMngService {

    /**
     * 查询用户列表
     * @param nickname
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate, Date endDate,
                                            Integer page, Integer pageSize);

    /**
     * 冻结/解冻用户账号
     * @param userId
     * @param status
     */
    public void freezeUserOrNot(String userId, Integer status);
}
