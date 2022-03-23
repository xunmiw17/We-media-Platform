package com.imooc.api;

import com.imooc.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    @Autowired
    public RedisOperator redisOperator;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
}
