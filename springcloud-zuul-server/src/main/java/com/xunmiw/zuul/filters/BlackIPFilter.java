package com.xunmiw.zuul.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.utils.IPUtil;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 构建Zuul自定义过滤器
 */
@Component
@RefreshScope
public class BlackIPFilter extends ZuulFilter {

    @Value("${blackIp.maxContinuousRequest}")
    private Integer maxContinuousRequest;

    @Value("${blackIp.requestInterval}")
    private Integer requestInterval;

    @Value("${blackIp.limitTime}")
    private Integer limitTime;

    @Autowired
    private RedisOperator redisOperator;

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
        return 2;
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

        System.out.println(maxContinuousRequest);
        System.out.println(requestInterval);
        System.out.println(limitTime);

        // 获得RequestContext上下文对象，以获得ip地址
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        String requestIP = IPUtil.getRequestIp(request);

        // 判断IP在规定时间内请求次数是否超过自定义阈值。如超过，则限制该IP访问一定时间，过后再放行
        final String ipRequestCountRedis = "zuul-ip:" + requestIP;
        final String ipTimeLimitRedis = "zuul-ip-limit" + requestIP;

        // 如果当前限制ip的key还存在剩余时间，说明这个ip不能访问，继续等待
        long limitLeftTime = redisOperator.ttl(ipTimeLimitRedis);
        if (limitLeftTime > 0) {
            blockRequest(context);
            return null;
        }

        // 在Redis中累加IP请求的访问次数。如果此IP为第一次访问，则设置过期时间为interval
        long requestCount = redisOperator.increment(ipRequestCountRedis, 1);
        if (requestCount == 1) {
            redisOperator.expire(ipRequestCountRedis, requestInterval);
        }

        // 一旦请求次数在interval内超过自定义阈值，则设置改IP的禁止访问间隔
        if (requestCount > maxContinuousRequest) {
            redisOperator.set(ipTimeLimitRedis, ipTimeLimitRedis, limitTime);
            blockRequest(context);
        }
        return null;
    }

    private void blockRequest(RequestContext context) {
        // 停止Zuul继续向下路由
        context.setSendZuulResponse(false);
        // 返回错误信息给用户端
        context.setResponseStatusCode(200);
        String result = JsonUtils.objectToJson(GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_ZUUL));
        context.setResponseBody(result);
        context.getResponse().setCharacterEncoding("utf-8");
        context.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
