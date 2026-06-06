package com.haifeng.app.vo.major;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** C 端专业类别统计 VO（spec 任务1接口3） */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MajorCategoryStatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String majorCategory;
    private Integer count;
}
