package com.haifeng.app.dto.university;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端大学→考研专业列表查询 DTO（spec 任务3接口1） */
@Data
@EqualsAndHashCode(callSuper = true)
public class UniversityPostgradMajorQueryDTO extends BasePageQueryDTO {

    /** 精准查询（学术学位 / 专业学位） */
    private String degreeType;
}
