package com.haifeng.admin.vo.home;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class InstitutionDetailVO {
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
