package com.xunmiw.exception;

import com.xunmiw.grace.result.ResponseStatusEnum;

/**
 * 优雅地处理异常
 */
public class GraceException {

    public static void display(ResponseStatusEnum responseStatusEnum) {
        throw new MyCustomException(responseStatusEnum);
    }
}
