package com.haifeng.app.dto.algorithm.article;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 热点资讯 - C 端标题关键字搜索 DTO（免登录）
 * <p>province 缺省/空白/字面量「全国」均视为不限省份；
 * limit 缺省 10，Service 内再钳制一次 [1,10] 防御。
 */
@Data
public class BizArticleSearchDTO {

    /** 用户原始输入（未去噪，归一化在匹配器内做） */
    @NotBlank(message = "关键字不能为空")
    @Size(max = 100, message = "关键字长度不能超过100")
    private String keyword;

    /** 模块作用域（高考/中考/考公/考研/英语四六级），null/空白 = 查全部类型 */
    @Size(max = 16, message = "模块名称长度不能超过16")
    private String moduleName;

    /** 省份精准过滤（裸值，如 山西）；空白或「全国」= 不限 */
    @Size(max = 16, message = "省份长度不能超过16")
    private String province;

    /** 返回条数，默认 10，最大 10 */
    @Min(value = 1, message = "limit最小为1")
    @Max(value = 10, message = "limit最大为10")
    private Integer limit;
}
