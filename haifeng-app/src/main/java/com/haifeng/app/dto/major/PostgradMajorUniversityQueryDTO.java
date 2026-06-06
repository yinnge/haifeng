package com.haifeng.app.dto.major;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端考研专业→大学列表查询 DTO（spec 任务4接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostgradMajorUniversityQueryDTO extends BasePageQueryDTO {

    /** 精准查询（综合/理工/师范/...） */
    private String category;
}
