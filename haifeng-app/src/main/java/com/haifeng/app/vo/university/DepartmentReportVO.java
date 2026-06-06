package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/** C 端院系分析报告 VO（spec §3.4，全 JSONB 透传） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
