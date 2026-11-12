package com.haifeng.app.vo.company;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    /** 雪花 ID：序列化为字符串，避免前端 JS Number 精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
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
