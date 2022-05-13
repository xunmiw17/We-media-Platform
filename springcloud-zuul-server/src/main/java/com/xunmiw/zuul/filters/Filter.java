package com.xunmiw.zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.stereotype.Component;

/**
 * 构建Zuul自定义过滤器 (玩具类)
 */
@Component
public class Filter extends ZuulFilter {

    /**
     * 定义过滤器类型
     * pre：请求被路由前执行
     * route：在路由请求时执行
     * post：请求路由后执行
     * error：处理请求时发生错误时执行
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行顺序（从小到大依次执行）
     * @return
     */
    @Override
    public int filterOrder() {
        return 1;
    }

    /**
     * 是否开启过滤器
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的业务实现
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        System.out.println("pre execute");
        return null;
    }
}
