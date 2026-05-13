package com.haifeng.app.service.impl.home;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.haifeng.app.service.home.SiteService;
import com.haifeng.app.vo.home.SiteInfoVO;
import com.haifeng.common.entity.system.SystemSettings;
import com.haifeng.common.mapper.system.SystemSettingsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private static final String CACHE_KEY = "haifeng:app:site-info";
    private static final long CACHE_TTL_HOURS = 1;

    private final SystemSettingsMapper settingsMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public SiteInfoVO getSiteInfo() {
        // 先查缓存
        SiteInfoVO cached = (SiteInfoVO) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        // 查数据库
        SystemSettings settings = settingsMapper.selectOne(
                new LambdaQueryWrapper<SystemSettings>().last("LIMIT 1"));

        if (settings == null) {
            return SiteInfoVO.builder().build();
        }

        SiteInfoVO vo = SiteInfoVO.builder()
                .siteIcp(settings.getSiteIcp())
                .contactUrl(settings.getContactUrl())
                .basicMessage(settings.getBasicMessage())
                .build();

        // 写入缓存
        redisTemplate.opsForValue().set(CACHE_KEY, vo, CACHE_TTL_HOURS, TimeUnit.HOURS);

        return vo;
    }
}
