package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * C 端院校列表 VO（任务 1）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private List<String> tags;
    private String cityName;
    private String educationLevel;
    private String provinceName;
    private String introduction;
    private String imageUrl;
    private String nature;
    private String category;
    private Integer majorCount;
    private Boolean hasDoctorate;
    private Boolean hasMaster;
    private String department;
}
