package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideDetailVO implements Serializable {

    private Long id;

    private Long universityId;

    private String universityName;

    private List<String> customTags;

    private Map<String, Object> campusFacilities;

    private Map<String, Object> dormitoryServices;

    private Map<String, Object> campusTransportation;

    private Map<String, Object> academicGuidance;

    private Map<String, Object> majorTransferGuidelines;

    private Map<String, Object> majorTransferConstriction;

    private Map<String, Object> academicSupportResources;

    private Map<String, Object> studentOrganizations;

    private Map<String, Object> campusEvents;

    private Map<String, Object> classDormSocial;

    private Map<String, Object> financialAid;

    private Map<String, Object> campusSecurity;

    private Map<String, Object> healthServices;

    private Map<String, Object> lifeServices;

    private String remark;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
