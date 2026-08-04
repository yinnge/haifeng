package com.haifeng.app.dto.employment.contentManagement.examGuide;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 备考指南分页查询 DTO（用户端）
 * 字段与前端 ExamGuideQueryDTO 对齐：keyword 由前端同时映射为 title/subtitle 两个参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExamGuideQueryDTO extends BasePageQueryDTO {

    @Size(max = 500, message = "标题长度不能超过500")
    private String title;

    @Size(max = 500, message = "副标题长度不能超过500")
    private String subtitle;

    @Size(max = 30, message = "指南类别长度不能超过30")
    private String guideCategory;

    @Size(max = 50, message = "指南类型长度不能超过50")
    private String guideType;

    @Size(max = 50, message = "难度等级长度不能超过50")
    private String difficultyLevel;

    @Size(max = 50, message = "作者头衔长度不能超过50")
    private String authorTitle;

    @Size(max = 50, message = "作者名称长度不能超过50")
    private String authorName;
}
