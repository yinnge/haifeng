package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端院校精简 VO（spec 任务4接口1，考研专业→大学列表） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityBriefForPostgradVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
}
