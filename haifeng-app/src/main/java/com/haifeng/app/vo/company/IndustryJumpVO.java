package com.haifeng.app.vo.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行业跳转信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryJumpVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long industryId;
    private String industryName;
}
