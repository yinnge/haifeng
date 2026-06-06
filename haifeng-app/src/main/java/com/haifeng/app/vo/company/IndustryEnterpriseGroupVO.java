package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 行业关联企业分组 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryEnterpriseGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long industryId;
    private List<EnterpriseJumpVO> enterprises;
}
