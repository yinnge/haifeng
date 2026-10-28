package com.haifeng.app.vo.university;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * C 端院校列表 VO（按界面原型重设计）
 * imageUrl=院校 logo；coverImage=详情轮播首图（detail 缺失或轮播为空时为 null，前端用占位图）；
 * content=院校简介（后端返全文，截断展示由前端处理）
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
    private String provinceName;
    private String cityName;
    private String region;
    private String category;
    private Integer majorCount;
    private String educationLevel;
    private String nature;
    private String imageUrl;
    private String coverImage;
    private String content;
}
