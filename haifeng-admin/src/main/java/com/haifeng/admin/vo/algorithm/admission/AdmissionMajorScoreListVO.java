package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.util.List;

@Data
public class AdmissionMajorScoreListVO {
    private Integer id;
    private Integer groupId;
    private String majorCode;
    private String majorName;
    private String educationLevel;
    private List<Object> history;
    private Boolean isDeleted;
}
