package com.haifeng.admin.dto.fileload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 热点资讯 - 修改 DTO（配合路径参数 id，整体表单提交）
 */
@Data
public class BizArticleUpdateDTO {

    /** 所属模块：高考/考公/考研/中考/英语四六级（与 DB CHECK 约束对齐） */
    @NotBlank(message = "所属模块不能为空")
    @Pattern(regexp = "高考|考公|考研|中考|英语四六级", message = "所属模块必须是：高考/考公/考研/中考/英语四六级")
    private String moduleName;

    /** 省份裸值（如 山西；全国=全国）；空=不限省份 */
    @Size(max = 16, message = "省份长度不能超过16")
    private String province;

    /** 年份（如 2026）；空=不分年份 */
    @Min(value = 2000, message = "年份最小为2000")
    @Max(value = 2100, message = "年份最大为2100")
    private Integer year;

    /** 专栏名 */
    @NotBlank(message = "专栏名不能为空")
    @Size(max = 64, message = "专栏名长度不能超过64")
    private String columnName;

    /** 标题 */
    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255")
    private String title;

    /** 列表页摘要 */
    @Size(max = 500, message = "摘要长度不能超过500")
    private String summary;

    /** 富文本 HTML 正文 */
    @NotBlank(message = "正文内容不能为空")
    private String content;

    /** 状态：0已发布 1下架 */
    @Min(value = 0, message = "状态取值只能为0或1")
    @Max(value = 1, message = "状态取值只能为0或1")
    private Integer status;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    /** 发布时间 */
    private OffsetDateTime publishTime;
}
