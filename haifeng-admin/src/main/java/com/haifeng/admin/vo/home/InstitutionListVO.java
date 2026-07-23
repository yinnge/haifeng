package com.haifeng.admin.vo.home;

import lombok.Data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class InstitutionListVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String type;
    private String phone;
    private String address;
    private String logo;
    private Integer sortOrder;
    private Short status;
}
