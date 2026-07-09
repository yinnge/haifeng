package com.haifeng.common.service.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderConfig {

    private Long id;

    @ToString.Exclude
    private String apiKey;

    private String baseUrl;

    private String modelName;

    private String providerName;
}
