package com.haifeng.admin.vo.university;

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
public class LaboratoryListVO implements Serializable {
    private Long id;
    private Long universityId;
    private String universityName;
    private String name;
    private String labType;
    private String region;
    private String department;
    private String director;
    private Integer status;
    private OffsetDateTime createdAt;
}
