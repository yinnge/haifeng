package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_institutions", autoResultMap = true)
public class Institution {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String type;

    private String phone;

    private String address;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> courses;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private String logo;

    private Integer sortOrder;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
