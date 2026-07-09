package com.haifeng.admin.service.impl.system;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haifeng.admin.service.system.AiBalanceService;
import com.haifeng.admin.vo.system.AiBalanceVO;
import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AiBalanceServiceImpl implements AiBalanceService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String CACHE_PREFIX = "haifeng:ai:balance:";
    private static final long CACHE_TTL_MINUTES = 5L;
    private static final Duration API_TIMEOUT = Duration.ofSeconds(10);

    private final ModelProviderMapper modelProviderMapper;
    private final WebClient webClient;
    private final DeepSeekProperties properties;
    private final StringRedisTemplate redisTemplate;

    public AiBalanceServiceImpl(ModelProviderMapper modelProviderMapper,
                                @Qualifier("deepSeekWebClient") WebClient webClient,
                                DeepSeekProperties properties,
                                StringRedisTemplate redisTemplate) {
        this.modelProviderMapper = modelProviderMapper;
        this.webClient = webClient;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<AiBalanceVO> getDeepSeekBalances(boolean refresh) {
        List<ModelProvider> providers = modelProviderMapper.findEnabledByProviderAndType("deepseek", "ai");
        if (providers.isEmpty()) {
            return List.of();
        }

        // 按 apiKey 去重，合并 models 列表
        Map<String, List<String>> apiKeyToModels = new LinkedHashMap<>();
        Map<String, String> apiKeyToBaseUrl = new LinkedHashMap<>();
        for (ModelProvider p : providers) {
            String key = p.getApiKey();
            apiKeyToModels.computeIfAbsent(key, k -> new ArrayList<>()).add(p.getModelName());
            apiKeyToBaseUrl.putIfAbsent(key, p.getBaseUrl());
        }

        List<AiBalanceVO> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : apiKeyToModels.entrySet()) {
            String apiKey = entry.getKey();
            List<String> models = entry.getValue();
            String baseUrl = apiKeyToBaseUrl.get(apiKey);
            result.add(getBalanceForKey(apiKey, models, baseUrl, refresh));
        }
        return result;
    }

    private AiBalanceVO getBalanceForKey(String apiKey, List<String> models, String providerBaseUrl, boolean refresh) {
        String cacheKey = CACHE_PREFIX + apiKey;

        if (!refresh) {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                try {
                    AiBalanceVO vo = MAPPER.readValue(cached, AiBalanceVO.class);
                    vo.setModels(models);
                    return vo;
                } catch (Exception e) {
                    log.warn("反序列化余额缓存失败, key={}", cacheKey, e);
                }
            }
        }

        AiBalanceVO vo = new AiBalanceVO();
        vo.setProviderName("deepseek");
        vo.setModels(models);

        try {
            String baseUrl = StringUtils.hasText(providerBaseUrl) ? providerBaseUrl : properties.getBaseUrl();
            String response = webClient.get()
                    .uri(baseUrl + "/user/balance")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(API_TIMEOUT);

            JsonNode root = MAPPER.readTree(response);
            vo.setIsAvailable(root.path("is_available").asBoolean(false));

            JsonNode infos = root.path("balance_infos");
            if (infos.isArray() && !infos.isEmpty()) {
                JsonNode info = infos.get(0);
                vo.setCurrency(textOrNull(info, "currency"));
                vo.setTotalBalance(decimalOrNull(info, "total_balance"));
                vo.setGrantedBalance(decimalOrNull(info, "granted_balance"));
                vo.setToppedUpBalance(decimalOrNull(info, "topped_up_balance"));
            }

            // 缓存成功结果
            try {
                String json = MAPPER.writeValueAsString(vo);
                redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("缓存余额失败, key={}", cacheKey, e);
            }
        } catch (Exception e) {
            log.warn("查询DeepSeek余额失败, apiKey={}", maskApiKey(apiKey), e);
            vo.setIsAvailable(false);
        }

        return vo;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        try {
            return new BigDecimal(value.asText());
        } catch (Exception e) {
            return null;
        }
    }

    private String maskApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey) || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
