package com.haifeng.app.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.haifeng.common.dto.home.PlannerServiceItem;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * C 端规划师详情 VO：基础信息 + 列表页展示 + 详情页展示。
 * 预约信息（邮箱/手机/地址/微信二维码）不在此接口，见 PlannerAppointmentVO。
 */
@Data
public class PlannerDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    // ===== 基础信息 =====
    private String name;
    private String serviceTarget;
    private String position;
    private String province;
    private String avatarUrl;

    // ===== 列表页展示 =====
    private String listDesc;
    private List<String> listTags;

    // ===== 详情页展示 =====
    private String workYears;
    private String servedCount;
    private String draftSla;
    private String certDesc;
    private String description;
    private List<String> detailTags;
    private List<PlannerServiceItem> services;
}
