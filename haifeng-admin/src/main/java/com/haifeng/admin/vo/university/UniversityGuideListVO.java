package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideListVO implements Serializable {

    private Long id;

    private Long universityId;

    private String universityName;

    private List<String> customTags;

    private String remark;

    private Integer status;

    private LocalDateTime createdAt;
}
