package com.haifeng.admin.vo.home;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.haifeng.common.entity.home.InstitutionClassType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class InstitutionDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String category;

    private String name;

    private String province;

    private String foundedYear;

    private List<String> carouselImages;

    private List<String> tags;

    private String introduction;

    private String mainDirection;

    private String classSize;

    private String teacherConfig;

    private String courseForm;

    private List<InstitutionClassType> classTypes;

    private String notice;

    private List<String> advantages;

    private String address;

    private String wechatQrcodeUrl;

    private String email;

    private String phone;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String managerUsername;

    private String managerPhone;

    private Short status;

    private Integer sortOrder;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
