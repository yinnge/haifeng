package com.haifeng.admin.vo.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionListVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String compName;

    private String compLevel;

    private String registrationTime;

    private Boolean isDeleted;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;

    /**
     * 排序权重（越大越靠前，C 端热点优先展示）
     */
    private Integer sortOrder;
}
