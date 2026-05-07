package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LaboratoryAddDTO {
    @NotNull(message = "院校ID不能为空")
    private Long universityId;
    @NotBlank(message = "实验室名称不能为空")
    private String name;
    @NotBlank(message = "实验室类型不能为空")
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
}
