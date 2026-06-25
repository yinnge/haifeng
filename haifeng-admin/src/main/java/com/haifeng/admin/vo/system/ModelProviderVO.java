package com.haifeng.admin.vo.system;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ModelProviderVO {

    private Long id;

    private String apiKeyMasked;

    private String modelName;

    private String providerName;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
