package com.haifeng.admin.dto.university;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DepartmentUpdateDTO {
    private String departmentName;
    private String departmentType;
    private String pageTitle;
    private List<String> tags;
    private Integer sortOrder;
    private Integer status;
    // 报告相关字段
    private String subtitle;
    private Map<String, Object> overview;
    private List<Map<String, Object>> subjectsDetail;
    private Map<String, Object> postgraduate;
    private List<Map<String, Object>> citySalary;
    private List<Map<String, Object>> salary;
    private List<Map<String, Object>> career;
    private Map<String, Object> trends;
    private Map<String, Object> prospects;
    private Map<String, Object> disclaimer;
    private List<Map<String, Object>> majorCompose;
}
