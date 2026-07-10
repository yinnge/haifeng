package com.haifeng.app.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * C 端企业列表查询 DTO
 * enterpriseName 走 LIKE；enterpriseNature / enterpriseType / cityName / recruitmentStatus 精准匹配（AND 组合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnterpriseQueryDTO extends BasePageQueryDTO {

    /** 企业名称模糊（LIKE %enterpriseName%） */
    @Size(max = 100, message = "企业名称长度不能超过100")
    private String enterpriseName;

    /** 企业性质精准匹配 */
    @Size(max = 20, message = "企业性质长度不能超过20")
    private String enterpriseNature;

    /** 企业类型精准匹配 */
    @Size(max = 20, message = "企业类型长度不能超过20")
    private String enterpriseType;

    /** 城市名称精准匹配 */
    @Size(max = 20, message = "城市名称长度不能超过20")
    private String cityName;

    /** 招聘状态精准匹配 */
    @Size(max = 20, message = "招聘状态长度不能超过20")
    private String recruitmentStatus;
}
