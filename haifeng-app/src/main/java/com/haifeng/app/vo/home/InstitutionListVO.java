package com.haifeng.app.vo.home;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * C 端培训机构列表项：只放列表卡片需要的字段。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstitutionListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String name;

    /**
     * 分类：高中冲刺小班/考公考编机构/央国企培训/职业培训机构
     */
    private String category;

    private String province;

    /**
     * 标签
     */
    private List<String> tags;
}
