package com.haifeng.admin.vo.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProviderVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String apiKeyMasked;
    private String baseUrl;
    private String modelName;
    private String providerName;
    private String type;
    private String description;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
