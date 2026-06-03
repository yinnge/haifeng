package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/** 任务 3.4 社交融入类 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideSocialVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, Object> studentOrganizations;
    private Map<String, Object> campusEvents;
    private Map<String, Object> classDormSocial;
}
