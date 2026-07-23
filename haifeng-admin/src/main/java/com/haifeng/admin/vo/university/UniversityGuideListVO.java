package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideListVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long universityId;

    private String universityName;

    private List<String> customTags;

    private String remark;

    private Integer status;

    private LocalDateTime createdAt;
}
