package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 企业关联行业分组 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseIndustryGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long enterpriseId;
    private List<IndustryJumpVO> industries;
}
