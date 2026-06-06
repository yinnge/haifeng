package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C 端企业列表 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String cityName;
    private String enterpriseName;
    private String enterpriseNature;
    private String enterpriseType;
    private String logoUrl;
    private String officialWebsite;
    private String region;
    private String enterpriseScale;
    private String mainBusiness;
    private String enterpriseIntro;
}
