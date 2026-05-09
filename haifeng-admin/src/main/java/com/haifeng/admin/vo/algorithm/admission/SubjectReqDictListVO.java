package com.haifeng.admin.vo.algorithm.admission;

import lombok.Data;
import java.util.List;

@Data
public class SubjectReqDictListVO {
    private Integer id;
    private String code;
    private String displayName;
    private Short requirementLevel;
    private List<String> subjects;
    private String requirementType;
    private Integer sortOrder;
}
