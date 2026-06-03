package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.2 基础生存类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSurvivalVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> campusFacilities;
    private Map<String, Object> dormitoryServices;
    private Map<String, Object> campusTransportation;
}
