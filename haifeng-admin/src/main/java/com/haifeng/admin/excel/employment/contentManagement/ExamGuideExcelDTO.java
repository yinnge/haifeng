package com.haifeng.admin.excel.employment.contentManagement;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExamGuideExcelDTO {
    @ExcelProperty("指南类别") private String guideCategory;
    @ExcelProperty("指南类型") private String guideType;
    @ExcelProperty("标题") private String title;
    @ExcelProperty("副标题") private String subtitle;
    @ExcelProperty("封面图片") private String coverImage;
    @ExcelProperty("图标类名") private String iconClass;
    @ExcelProperty("摘要") private String summary;
    @ExcelProperty("详细内容") private String content;
    @ExcelProperty("标签(逗号分隔)") private String tags;
    @ExcelProperty("难度") private String difficultyLevel;
    @ExcelProperty("目标读者") private String targetAudience;
    @ExcelProperty("作者名") private String authorName;
    @ExcelProperty("作者头衔") private String authorTitle;
    @ExcelProperty("是否置顶") private Boolean isTop;
    @ExcelProperty("是否推荐") private Boolean isRecommended;
    @ExcelProperty("排序权重") private Integer sortOrder;
}
