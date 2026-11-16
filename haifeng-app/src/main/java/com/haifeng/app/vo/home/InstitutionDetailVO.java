package com.haifeng.app.vo.home;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.haifeng.common.entity.home.InstitutionClassType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * C 端培训机构详情 VO：只到 advantages。
 * <p>地址/微信二维码/邮箱/手机号等联系方式不在详情接口，见 {@link InstitutionAppointmentVO}。</p>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstitutionDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 分类：高中冲刺小班/考公考编机构/央国企培训/职业培训机构
     */
    private String category;

    private String name;

    private String province;

    /**
     * 创办时间，如 2013年
     */
    private String foundedYear;

    /**
     * 轮播图片
     */
    private List<String> carouselImages;

    /**
     * 标签
     */
    private List<String> tags;

    private String introduction;

    /**
     * 主打方向
     */
    private String mainDirection;

    /**
     * 班级规模
     */
    private String classSize;

    /**
     * 师资配置
     */
    private String teacherConfig;

    /**
     * 上课形式
     */
    private String courseForm;

    /**
     * 主打班型
     */
    private List<InstitutionClassType> classTypes;

    /**
     * 说明/温馨提示
     */
    private String notice;

    /**
     * 机构优势
     */
    private List<String> advantages;
}
