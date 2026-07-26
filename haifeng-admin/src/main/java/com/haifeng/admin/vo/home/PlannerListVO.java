package com.haifeng.admin.vo.home;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class PlannerListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String position;
    private String region;
    private String avatar;
    private String specialty;
    private String douyinName;
    private String douyinUrl;
    private Integer sortOrder;
    private Short status;
}
