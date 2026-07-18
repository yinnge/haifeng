package com.haifeng.common.entity.algorithm;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 省份高考改革配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_province_reform")
public class ProvinceReform {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 省份名称
     */
    private String province;

    /**
     * 改革年份
     */
    private Short reformYear;

    /**
     * 改革模式（如：3+3、3+1+2）
     */
    private String reformModel;

    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;

    @TableLogic
    private Boolean isDeleted;

    @Version
    private Integer version;
}
