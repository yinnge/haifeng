package com.haifeng.app.vo.company;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;
    private List<IndustryJumpVO> industries;
}
