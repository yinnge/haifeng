package com.haifeng.app.dto.algorithm.article;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 热点资讯 - C 端分页查询 DTO
 * <p>province 精准查询（取自用户高考档案的省份裸值，前端档案变更时需重新拉取列表）；
 * module_name 由各模块 Controller 固定传入，不由前端传。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BizArticleQueryDTO extends BasePageQueryDTO {

    /** 省份精准查询（裸值，如 山西；全国） */
    @NotBlank(message = "省份不能为空")
    @Size(max = 16, message = "省份长度不能超过16")
    private String province;
}
