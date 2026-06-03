package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.3 学业规划类（@RequirePro） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideAcademicVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> academicGuidance;
    private Map<String, Object> majorTransferGuidelines;
    private Map<String, Object> majorTransferConstriction;
    private Map<String, Object> academicSupportResources;
}
