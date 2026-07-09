package com.haifeng.common.aspect;

import com.haifeng.common.annotation.RateLimit;
import com.haifeng.common.exception.BusinessException;
import com.haifeng.common.response.ResultCode;
import com.haifeng.common.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String LIMIT_KEY_PREFIX = "haifeng:limit:api:";

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        long maxCount = rateLimit.value();
        long timeWindow = rateLimit.time();

        String ip = IpUtil.getClientIp();
        String requestPath = getRequestPath();
        String redisKey = LIMIT_KEY_PREFIX + ip + ":" + requestPath;

        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, timeWindow, TimeUnit.SECONDS);
        }

        if (count != null && count > maxCount) {
            log.warn("接口请求过于频繁, ip={}, path={}, count={}", ip, requestPath, count);
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }

        return joinPoint.proceed();
    }

    private String getRequestPath() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getMethod() + ":" + request.getRequestURI();
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }
}
