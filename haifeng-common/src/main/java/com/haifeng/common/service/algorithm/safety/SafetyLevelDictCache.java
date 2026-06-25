package com.haifeng.common.service.algorithm.safety;

import com.haifeng.common.entity.algorithm.SafetyLevelDict;
import com.haifeng.common.mapper.algorithm.SafetyLevelDictMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 安全系数字典缓存
 * <p>
 * t_safety_level_dict 只有 5 行,缓存到内存避免每次反查 DB
 * 启动加载 + 30 分钟定时刷新
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SafetyLevelDictCache {

    private final SafetyLevelDictMapper safetyLevelDictMapper;

    private final AtomicReference<List<SafetyLevelDict>> cacheRef = new AtomicReference<>(Collections.emptyList());

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000L, initialDelay = 30 * 60 * 1000L)
    public void refresh() {
        try {
            List<SafetyLevelDict> all = safetyLevelDictMapper.selectAll();
            if (all != null && !all.isEmpty()) {
                all.sort(Comparator.comparing(SafetyLevelDict::getLevel));
                cacheRef.set(Collections.unmodifiableList(all));
                log.info("安全系数字典缓存刷新成功,条目数={}", all.size());
            } else {
                log.warn("安全系数字典加载为空,保留旧缓存");
            }
        } catch (Exception e) {
            log.error("安全系数字典缓存刷新失败", e);
        }
    }

    /**
     * 获取所有字典(已按 level 升序)
     */
    public List<SafetyLevelDict> getAll() {
        return cacheRef.get();
    }

    /**
     * 根据安全系数反查字典
     *
     * @param coefficient 0.00~1.00
     * @return 匹配的字典项,未匹配返回 null
     */
    public SafetyLevelDict getByCoefficient(BigDecimal coefficient) {
        if (coefficient == null) {
            return null;
        }
        List<SafetyLevelDict> all = cacheRef.get();
        if (all.isEmpty()) {
            return null;
        }
        Optional<SafetyLevelDict> match = all.stream()
                .filter(d -> d.getMinCoefficient() != null && d.getMaxCoefficient() != null)
                .filter(d -> coefficient.compareTo(d.getMinCoefficient()) >= 0
                        && coefficient.compareTo(d.getMaxCoefficient()) < 0)
                .findFirst();
        return match.orElse(null);
    }
}
