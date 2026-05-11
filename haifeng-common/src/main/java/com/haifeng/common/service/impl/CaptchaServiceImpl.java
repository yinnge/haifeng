package com.haifeng.common.service.impl;

import com.haifeng.common.constant.RedisKeyConstant;
import com.haifeng.common.service.CaptchaService;
import com.haifeng.common.vo.auth.CaptchaVO;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private static final int CAPTCHA_WIDTH = 130;
    private static final int CAPTCHA_HEIGHT = 48;
    private static final int CAPTCHA_LENGTH = 4;
    private static final long CAPTCHA_EXPIRE_MINUTES = 2;

    private final StringRedisTemplate redisTemplate;

    @Override
    public CaptchaVO generateCaptcha() {
        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(CAPTCHA_WIDTH, CAPTCHA_HEIGHT, CAPTCHA_LENGTH);
        String code = captcha.text().toLowerCase();
        String uuid = UUID.randomUUID().toString();

        // 存入 Redis，2 分钟过期
        String redisKey = RedisKeyConstant.getCaptchaKey(uuid);
        redisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        log.debug("验证码已生成, uuid={}", uuid);

        return CaptchaVO.builder()
                .uuid(uuid)
                .image(captcha.toBase64())
                .build();
    }

    @Override
    public boolean validateCaptcha(String uuid, String code) {
        if (!StringUtils.hasText(uuid) || !StringUtils.hasText(code)) {
            return false;
        }

        String redisKey = RedisKeyConstant.getCaptchaKey(uuid);
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            log.warn("验证码已过期或不存在, uuid={}", uuid);
            return false;
        }

        // 验证后立即删除（一次性使用）
        redisTemplate.delete(redisKey);

        // 忽略大小写比较
        boolean valid = storedCode.equalsIgnoreCase(code);
        if (!valid) {
            log.warn("验证码错误, uuid={}, expected={}, actual={}", uuid, storedCode, code);
        }

        return valid;
    }
}
