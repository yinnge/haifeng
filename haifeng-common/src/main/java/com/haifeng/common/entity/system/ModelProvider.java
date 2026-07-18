package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_model_provider")
public class ModelProvider {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ToString.Exclude
    private String apiKey;

    private String baseUrl;

    private String modelName;

    private String providerName;

    private String type;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
