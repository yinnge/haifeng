package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionMajorScoreListVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private String duration;
    private String tuition;
    private String description;
    private List<Map<String, Object>> history;
    private List<String> constraints;
}
