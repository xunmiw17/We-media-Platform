package com.xunmiw.api.interceptors;

import com.xunmiw.api.BaseController;
import com.xunmiw.exception.GraceException;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PassportInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisOperator redisOperator;

    /**
     * 拦截请求，访问Controller之前
     * 如果Redis中用户IP仍然存在，则表示60s事件未到，拦截请求
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIP = IPUtil.getRequestIp(request);
        if (redisOperator.keyIsExist(BaseController.MOBILE_SMSCODE + ":" + userIP)) {
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
            System.out.println("短信发送频率过高");
            return false;
        }
        /**
         * false：请求被拦截
         * true：请求通过验证并放行
         */
        return true;
    }

    /**
     * 请求访问到Controller之后，渲染视图之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 请求访问到Controller之后，渲染视图之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
