package com.haifeng.admin.dto.university;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class LaboratoryAddDTO {
    @NotNull(message = "院校ID不能为空")
    private Long universityId;
    @NotBlank(message = "实验室名称不能为空")
    @Size(max = 100, message = "实验室名称长度不能超过100")
    private String name;
    @NotBlank(message = "实验室类型不能为空")
    @Size(max = 50, message = "实验室类型长度不能超过50")
    private String labType;
    @Size(max = 20, message = "成立时间长度不能超过20")
    private String establishedYear;
    @Size(max = 50, message = "所在地区长度不能超过50")
    private String region;
    @Size(max = 50, message = "主管部门长度不能超过50")
    private String department;
    @Size(max = 50, message = "实验室主任长度不能超过50")
    private String director;
    @Size(max = 20, message = "人员规模长度不能超过20")
    private String staffCount;
    @Size(max = 20, message = "学生规模长度不能超过20")
    private String studentCount;
    @Size(max = 100, message = "联系邮箱长度不能超过100")
    private String email;
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;
    private String introduction;
    private String researchDescription;
    @Size(max = 200, message = "实验室空间长度不能超过200")
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
