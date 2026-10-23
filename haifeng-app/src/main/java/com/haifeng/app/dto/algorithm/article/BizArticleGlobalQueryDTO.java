package com.haifeng.app.dto.algorithm.article;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热点资讯 - C 端全局分页查询 DTO（跨模块统一入口）
 * <p>moduleName 精准查询（可空，空=查全部模块：考研/考公/中考/英语四六级等）；
 * province 精准过滤（可空，空/「全国」= 不限省份）。两者均由前端传入，Controller 不再写死模块。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizArticleGlobalQueryDTO extends BasePageQueryDTO {

    /** 模块精准查询（高考/中考/考公/考研/英语四六级），可空=全部模块 */
    @Size(max = 16, message = "模块名称长度不能超过16")
    private String moduleName;

    /** 省份精准过滤（裸值，如 山西），可空=不限省份（含「全国」与未设省份） */
    @Size(max = 16, message = "省份长度不能超过16")
    private String province;
}
