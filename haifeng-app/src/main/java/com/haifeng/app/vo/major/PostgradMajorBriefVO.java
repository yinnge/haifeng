package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端考研专业精简 VO（spec 任务3接口1，大学→考研专业列表） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostgradMajorBriefVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String majorName;
    private String degreeType;
}
