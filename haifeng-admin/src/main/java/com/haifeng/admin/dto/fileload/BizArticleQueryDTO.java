package com.haifeng.admin.dto.fileload;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热点资讯 - 分页查询 DTO
 * <p>年份/省份/模块名/专栏名精准查询，标题模糊查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizArticleQueryDTO extends BasePageQueryDTO {

    /** 模块名精准（高考/考公/考研/中考/英语四六级） */
    @Size(max = 32, message = "模块名长度不能超过32")
    private String moduleName;

    /** 省份精准（裸值，如 山西；全国） */
    @Size(max = 16, message = "省份长度不能超过16")
    private String province;

    /** 年份精准 */
    @Min(value = 2000, message = "年份最小为2000")
    @Max(value = 2100, message = "年份最大为2100")
    private Integer year;

    /** 专栏名精准 */
    @Size(max = 64, message = "专栏名长度不能超过64")
    private String columnName;

    /** 标题模糊 */
    @Size(max = 100, message = "标题长度不能超过100")
    private String title;
}
