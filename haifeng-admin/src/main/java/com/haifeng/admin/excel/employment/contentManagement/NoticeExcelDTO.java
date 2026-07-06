package com.haifeng.admin.excel.employment.contentManagement;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class NoticeExcelDTO {
    @ExcelProperty("公告类别") private String noticeCategory;
    @ExcelProperty("公告类型") private String noticeType;
    @ExcelProperty("标题") private String title;
    @ExcelProperty("摘要") private String summary;
    @ExcelProperty("公告内容") private String content;
    @ExcelProperty("省份") private String province;
    @ExcelProperty("城市") private String city;
    @ExcelProperty("标签(逗号分隔)") private String tags;
    @ExcelProperty("年份") private String year;
    @ExcelProperty("来源") private String source;
    @ExcelProperty("原文链接") private String sourceUrl;
    @ExcelProperty("发布日期") private OffsetDateTime publishDate;
    @ExcelProperty("发布单位") private String publishUnit;
    @ExcelProperty("报名开始日期") private OffsetDateTime regStartDate;
    @ExcelProperty("报名截止日期") private OffsetDateTime regEndDate;
    @ExcelProperty("考试时间") private OffsetDateTime examTime;
    @ExcelProperty("招录总人数") private Integer recruitmentCount;
    @ExcelProperty("是否置顶") private Boolean isTop;
    @ExcelProperty("是否重要") private Boolean isImportant;
    @ExcelProperty("排序权重") private Integer sortOrder;
}
