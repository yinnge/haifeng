package com.haifeng.common.entity.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    @TableId(type = IdType.AUTO)
    private Long id;

    @ToString.Exclude
    private String apiKey;

    private String modelName;

    private String providerName;

    private Integer status;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
