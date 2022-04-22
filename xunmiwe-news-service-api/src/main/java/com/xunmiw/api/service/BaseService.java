package com.xunmiw.api.service;

import com.github.pagehelper.PageInfo;
import com.xunmiw.utils.PagedGridResult;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BaseService {

    @Autowired
    public RedisOperator redisOperator;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_CATEGORY_LIST = "redis_category_list";
    public static final String REDIS_WRITER_FANS_COUNT = "redis_writer_fans_count";
    public static final String REDIS_USER_FOLLOW_COUNT = "redis_user_follow_count";
    public static final String REDIS_ARTICLE_COMMENT_COUNT = "redis_article_comment_count";

    public PagedGridResult setPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageInfo = new PageInfo<>(list);

        PagedGridResult pagedGridResult = new PagedGridResult();
        pagedGridResult.setRows(list);
        pagedGridResult.setPage(page);
        pagedGridResult.setRecords(pageInfo.getTotal());
        pagedGridResult.setTotal(pageInfo.getPages());

        return pagedGridResult;
    }
}
