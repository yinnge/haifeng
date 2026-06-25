package com.haifeng.common.service.ai;

import com.haifeng.common.config.DeepSeekProperties;
import com.haifeng.common.entity.system.ModelProvider;
import com.haifeng.common.mapper.system.ModelProviderMapper;
import com.haifeng.common.service.ai.dto.ModelProviderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ApiKeyPoolTest {

    private DeepSeekProperties properties;
    @SuppressWarnings("unchecked")
    private RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    private ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
    private ModelProviderMapper modelProviderMapper = mock(ModelProviderMapper.class);

    @BeforeEach
    void setup() {
        properties = new DeepSeekProperties();
        properties.setKeyCooldownSeconds(300);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private ApiKeyPool buildPool(List<ModelProvider> providers) {
        when(modelProviderMapper.findEnabledByProvider("deepseek")).thenReturn(providers);
        ApiKeyPool pool = new ApiKeyPool(properties, redisTemplate, modelProviderMapper);
        pool.init();
        return pool;
    }

    private ModelProvider provider(Long id, String key, String modelName) {
        return ModelProvider.builder()
                .id(id)
                .apiKey(key)
                .modelName(modelName)
                .providerName("deepseek")
                .status(1)
                .build();
    }

    @Test
    void emptyProviders_doesNotFailOnInitAndReturnsEmptyList() {
        ApiKeyPool pool = buildPool(Collections.emptyList());

        assertThat(pool.orderedFallback(1L)).isEmpty();
        assertThat(pool.pickProvider(1L)).isNull();
        assertThat(pool.pickKey(1L)).isNull();
    }

    @Test
    void orderedFallback_loadsEnabledProvidersFromDatabaseEachCall() {
        ApiKeyPool pool = buildPool(Collections.singletonList(provider(1L, "key-1", "deepseek-chat")));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<ModelProviderConfig> first = pool.orderedFallback(0L);
        assertThat(first).extracting(ModelProviderConfig::getId).containsExactly(1L);

        when(modelProviderMapper.findEnabledByProvider("deepseek")).thenReturn(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, "key-2", "deepseek-reasoner")
        ));

        List<ModelProviderConfig> second = pool.orderedFallback(0L);
        assertThat(second).extracting(ModelProviderConfig::getId).containsExactly(1L, 2L);
        verify(modelProviderMapper, times(2)).findEnabledByProvider("deepseek");
    }

    @Test
    void orderedFallback_filtersBlankApiKeyAndModelName() {
        ApiKeyPool pool = buildPool(Arrays.asList(
                provider(1L, "key-1", "deepseek-chat"),
                provider(2L, " ", "deepseek-chat"),
                provider(3L, "key-3", " "),
                provider(4L, null, "deepseek-chat"),
                provider(5L, "key-5", null)
        ));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<ModelProviderConfig> ordered = pool.orderedFallback(0L);

        assertThat(ordered).hasSize(1);
        assertThat(ordered.get(0).getId()).isEqualTo(1L);
        assertThat(ordered.get(0).getApiKey()).isEqualTo("key-1");
        assertThat(ordered.get(0).getModelName()).isEqualTo("deepseek-chat");
    }

    @Test
    void orderedFallback_ordersProvidersByUserIdModuloSize() {
        ApiKeyPool pool = buildPool(Arrays.asList(
                provider(1L, "key-1", "model-1"),
                provider(2L, "key-2", "model-2"),
                provider(3L, "key-3", "model-3")
        ));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<ModelProviderConfig> ordered = pool.orderedFallback(4L);

        assertThat(ordered).extracting(ModelProviderConfig::getId).containsExactly(2L, 3L, 1L);
    }

    @Test
    void orderedFallback_findsAdminNormalizedProviderName() {
        ModelProvider provider = provider(1L, "key-1", "deepseek-chat");
        provider.setProviderName("deepseek");
        ApiKeyPool pool = buildPool(Collections.singletonList(provider));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        List<ModelProviderConfig> ordered = pool.orderedFallback(0L);

        assertThat(ordered).hasSize(1);
        assertThat(ordered.get(0).getProviderName()).isEqualTo("deepseek");
        verify(modelProviderMapper, atLeastOnce()).findEnabledByProvider("deepseek");
    }

    @Test
    void pickProvider_returnsSameFirstProviderForSameUserId() {
        ApiKeyPool pool = buildPool(Arrays.asList(
                provider(1L, "key-1", "model-1"),
                provider(2L, "key-2", "model-2"),
                provider(3L, "key-3", "model-3")
        ));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(false);

        ModelProviderConfig first = pool.pickProvider(123L);
        ModelProviderConfig second = pool.pickProvider(123L);

        assertThat(first).isNotNull();
        assertThat(second).isNotNull();
        assertThat(first.getId()).isEqualTo(second.getId());
    }

    @Test
    void markUnhealthy_writesCooldownToRedisByProviderId() {
        ApiKeyPool pool = buildPool(Collections.singletonList(provider(1L, "key-1", "deepseek-chat")));
        ModelProviderConfig provider = ModelProviderConfig.builder()
                .id(1L)
                .apiKey("key-1")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .build();

        pool.markUnhealthy(provider);

        verify(valueOps).set(eq("ai:model-provider:cooldown:1"), eq("1"), eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void orderedFallbackAndPickProvider_skipCooldownProvider() {
        ApiKeyPool pool = buildPool(Arrays.asList(
                provider(1L, "key-1", "model-1"),
                provider(2L, "key-2", "model-2")
        ));
        when(redisTemplate.hasKey("ai:model-provider:cooldown:1")).thenReturn(true);
        when(redisTemplate.hasKey("ai:model-provider:cooldown:2")).thenReturn(false);

        List<ModelProviderConfig> ordered = pool.orderedFallback(0L);
        ModelProviderConfig picked = pool.pickProvider(0L);

        assertThat(ordered).extracting(ModelProviderConfig::getId).containsExactly(2L);
        assertThat(picked).isNotNull();
        assertThat(picked.getId()).isEqualTo(2L);
    }

    @Test
    void allProvidersCooldown_returnsEmptyList() {
        ApiKeyPool pool = buildPool(Arrays.asList(
                provider(1L, "key-1", "model-1"),
                provider(2L, "key-2", "model-2")
        ));
        when(redisTemplate.hasKey(ArgumentMatchers.anyString())).thenReturn(true);

        assertThat(pool.orderedFallback(123L)).isEmpty();
        assertThat(pool.pickProvider(123L)).isNull();
    }
}
