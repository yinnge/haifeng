package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UniversityGuideListVO {

    private Long id;

    private Long universityId;

    private String universityName;

    private List<String> customTags;

    private String remark;

    private Integer status;

    private LocalDateTime createdAt;
}
