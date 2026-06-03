package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.5 权益与安全类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSafetyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> financialAid;
    private Map<String, Object> campusSecurity;
    private Map<String, Object> healthServices;
}
