package com.haifeng.app.vo.company;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 企业跳转信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseJumpVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long enterpriseId;
    private String enterpriseName;
}
