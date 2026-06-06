package com.haifeng.app.dto.company;

import com.haifeng.common.dto.common.BasePageQueryDTO;
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
    private String enterpriseName;

    /** 企业性质精准匹配 */
    private String enterpriseNature;

    /** 企业类型精准匹配 */
    private String enterpriseType;

    /** 城市名称精准匹配 */
    private String cityName;

    /** 招聘状态精准匹配 */
    private String recruitmentStatus;
}
