package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 任务 3.1 概览：指南自定义标签 + 院校简要信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityGuideOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> customTags;

    // 来自 t_universities
    private String name;
    private List<String> tags;
    private String region;
    private String category;
    private String nature;
    private String imageUrl;
}
