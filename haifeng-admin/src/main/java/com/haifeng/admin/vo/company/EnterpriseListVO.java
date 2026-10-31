package com.haifeng.admin.vo.company;

import lombok.Data;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class EnterpriseListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String recruitmentStatus;
    private Boolean isDeleted;
    private LocalDateTime createdAt;

    /**
     * 排序权重（越大越靠前，C 端热点优先展示）
     */
    private Integer sortOrder;
}
