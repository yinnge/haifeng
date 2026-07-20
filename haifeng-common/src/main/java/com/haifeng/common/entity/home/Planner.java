package com.haifeng.common.entity.home;

import com.baomidou.mybatisplus.annotation.*;
import com.haifeng.common.config.StringListTypeHandler;
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
@TableName(value = "t_planners", autoResultMap = true)
public class Planner {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String position;

    private String region;

    private String avatar;

    private String specialty;

    private String douyinName;

    private String douyinUrl;

    private String personalDescription;

    private String experienceJob;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> achievements;

    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> expertiseAreas;

    private Integer sortOrder;

    private Short status;

    @TableLogic
    @TableField("is_deleted")
    private Boolean deleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
