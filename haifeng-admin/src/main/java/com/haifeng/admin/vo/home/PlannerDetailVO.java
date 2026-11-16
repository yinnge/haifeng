package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.haifeng.common.dto.home.PlannerServiceItem;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class PlannerDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String name;
    private String serviceTarget;
    private String position;
    private String province;
    private String listDesc;
    private List<String> listTags;
    private String avatarUrl;
    private String workYears;
    private String servedCount;
    private String draftSla;
    private String certDesc;
    private String description;
    private List<String> detailTags;
    private List<PlannerServiceItem> services;
    private String email;
    private String phone;
    private String address;
    private String wechatQrcodeUrl;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
