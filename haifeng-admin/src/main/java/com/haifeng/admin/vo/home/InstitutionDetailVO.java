package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class InstitutionDetailVO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String type;
    private String phone;
    private String address;
    private String description;
    private List<String> courses;
    private List<String> images;
    private String logo;
    private Integer sortOrder;
    private Short status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
