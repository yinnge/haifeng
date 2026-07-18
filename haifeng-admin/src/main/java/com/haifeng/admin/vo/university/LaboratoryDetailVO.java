package com.haifeng.admin.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryDetailVO implements Serializable {
    private Long id;
    private Long universityId;
    private String universityName;
    private String name;
    private String labType;
    private String establishedYear;
    private String region;
    private String department;
    private String director;
    private String staffCount;
    private String studentCount;
    private String email;
    private String phone;
    private String introduction;
    private String researchDescription;
    private String labSpace;
    private String openTopics;
    private String cooperation;
    private String visitingScholars;
    private List<String> researchFields;
    private List<Map<String, Object>> statistics;
    private List<String> majorEquipment;
    private List<Map<String, Object>> coreTeam;
    private Integer sortOrder;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
