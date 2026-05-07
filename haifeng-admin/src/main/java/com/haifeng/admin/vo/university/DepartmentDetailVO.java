package com.haifeng.admin.vo.university;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class DepartmentDetailVO {
    private Long id;
    private Long universityId;
    private String universityName;
    private String departmentName;
    private String departmentType;
    private String pageTitle;
    private List<String> tags;
    private Integer sortOrder;
    private Integer status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    // 报告相关字段
    private Long reportId;
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
