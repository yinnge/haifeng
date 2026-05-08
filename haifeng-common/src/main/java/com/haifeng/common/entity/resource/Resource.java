package com.haifeng.common.entity.resource;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_resource", autoResultMap = true)
public class Resource {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String resourceName;

    private String coverUrl;

    private String description;

    private String resourceUrl;

    private String accessCode;

    private String category;

    private String fileType;

    private Integer viewCount;

    private Integer sortOrder;

    private Boolean isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}
