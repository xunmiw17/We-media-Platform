package com.xunmiw.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceLogAspect {

    final static Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    /**
     * AOP通知
     * 1. 前置通知
     * 2. 后置通知
     * 3. 环绕通知
     * 4. 异常通知
     * 5. 最终通知
     */

    /**
     * 第一个* - 匹配所有返回类型
     * 第二个* - 表示任何字符串（所有包的service）
     * impl.. - 表示匹配impl包以及其下所有子包
     * 第三个* - 表示impl包及其子包下的所有类
     * 第四个* - 表示这些类中的所有方法
     * (..) - 表示匹配任意参数的方法
     */
    @Around("execution(* com.xunmiw.*.service.impl..*.*(..))")
    public Object recordTimeOfService(ProceedingJoinPoint pjp) throws Throwable {
        // 打印类名及方法名称
        logger.info("=== 开始执行 {}.{}===", pjp.getTarget().getClass(), pjp.getSignature().getName());
        long start = System.currentTimeMillis();

        // 执行目标方法
        Object result = pjp.proceed();

        long end = System.currentTimeMillis();
        long diff = end - start;
        if (diff > 3000) {
            logger.error("当前执行耗时: {}", diff);
        } else if (diff > 2000) {
            logger.warn("当前执行耗时: {}", diff);
        } else {
            logger.info("当前执行耗时: {}", diff);
        }
        return result;
    }
}
