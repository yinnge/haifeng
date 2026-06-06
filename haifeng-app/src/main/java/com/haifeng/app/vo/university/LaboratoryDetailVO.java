package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端实验室详情 VO（spec §3.2，20 字段，含 4 个 JSONB） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LaboratoryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String universityName;
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

    // JSONB 字段，原样透传给前端
    private List<String> researchFields;
    private List<Map<String, Object>> statistics;
    private List<String> majorEquipment;
    private List<Map<String, Object>> coreTeam;
}
