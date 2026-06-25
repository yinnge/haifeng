package com.haifeng.common.service.ai.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelProviderConfigTest {

    @Test
    void builderShouldPopulateAllFields() {
        ModelProviderConfig config = ModelProviderConfig.builder()
                .id(1L)
                .apiKey("test-api-key")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .build();

        assertThat(config.getId()).isEqualTo(1L);
        assertThat(config.getApiKey()).isEqualTo("test-api-key");
        assertThat(config.getModelName()).isEqualTo("deepseek-chat");
        assertThat(config.getProviderName()).isEqualTo("deepseek");
    }

    @Test
    void toStringShouldNotIncludeApiKey() {
        ModelProviderConfig config = ModelProviderConfig.builder()
                .id(1L)
                .apiKey("secret-api-key")
                .modelName("deepseek-chat")
                .providerName("deepseek")
                .build();

        assertThat(config.toString()).doesNotContain("secret-api-key");
        assertThat(config.toString()).doesNotContain("apiKey");
    }
}
